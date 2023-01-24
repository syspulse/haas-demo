package io.syspulse.haas.intercept

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult, Materializer}
import akka.stream.scaladsl.{Sink, Source, StreamConverters,Flow}
import akka.util.ByteString
import akka.stream.scaladsl.Framing
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

import com.typesafe.scalalogging.Logger

// ATTENTION
import io.syspulse.skel.service.JsonCommon
import spray.json._
import spray.json.{DefaultJsonProtocol,NullOptions}

import java.time.LocalDateTime
import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success

import io.syspulse.haas.core.Tx

import io.syspulse.haas.ingest.eth.EthEtlJson

import io.syspulse.haas.intercept.script.Script
import io.syspulse.haas.intercept.script._

import io.syspulse.haas.intercept.alarm.Alarms

import io.syspulse.haas.intercept.store.InterceptionStore
import io.syspulse.haas.intercept.store.ScriptStore
import scala.util.Failure

abstract class Interceptor[T](interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions0:Seq[Interception]) {
  protected val log = Logger(s"${this.getClass()}")
  
  //scripts:Seq[String],alarmUri:Seq[String]

  import EthEtlJson._
  import DefaultJsonProtocol._

  @volatile
  var interceptions:Map[Interception.ID,Interception] = 
    interceptionStore
      .all
      .filter(ix => ix.entity == entity())
      .map(ix => ix.id -> ix)
      .toMap ++
    interceptions0
      .filter(ix => ix.entity == entity())
      .map(ix => ix.id -> ix)
      .toMap

  def entity():String

  def +(ix:Interception) = {
    interceptions = interceptions + (ix.id -> ix)
  }
  def -(id:Interception.ID) = {
    interceptions = interceptions - (id)
  }

  def stop(id:Interception.ID) = {
    // mutable, nothing to do
  }

  def start(id:Interception.ID) = {
    // mutable, nothing to do
  }

  log.info(s"interceptions: ${interceptions}")

  val alarms = new Alarms(alarmThrottle)
  
  def decode(t:T):Map[String,Any]
 
  def scan(t:T):Seq[InterceptionAlarm] = {
    val onchainData = decode(t)

    val ii:Seq[InterceptionAlarm] = interceptions
      .values
      .filter(_.status == "started")
      .flatMap( ix => {
        log.debug(s"${ix} => ${scriptStore.?(ix.scriptId)}")
      
        scriptStore.?(ix.scriptId) match {
          case Success(script) => {
            
            val engine = ScriptEngine.engines.get(script.typ)
            if(engine.isDefined) {
              val r = engine.get.run(script.src,onchainData)
              log.debug(s"${t}: ${script}: ${Console.YELLOW}${r}${Console.RESET}")
              
              if(! r.isDefined) 
                None
              else {
                
                ix.++(1)

                val scriptOutput = s"${r.get}"
                val ia = InterceptionAlarm(
                    System.currentTimeMillis(),
                    ix.id,
                    onchainData.get("block_number").getOrElse(0L).asInstanceOf[Long].toLong,
                    onchainData.get("hash").getOrElse("").asInstanceOf[String],
                    scriptOutput,
                    alarm = ix.alarm)
                
                // memorize 
                interceptionStore.remember(ix,ia)

                Some(ia)
              }
            } else {
              log.error(s"engine not fonud: ${script.typ}")
              None
            }          
          }
          case Failure(e) => None
        }            
      }).toSeq

    ii.foreach{ 
      ia => alarms.send(ia) 
    }
    ii
  }
}