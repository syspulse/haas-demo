package io.syspulse.haas.ingest.eth.intercept

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
import io.syspulse.haas.ingest.eth.script.Script
//import io.syspulse.haas.ingest.eth.script.ScriptTrigger
import io.syspulse.haas.ingest.eth.alarm.Alarms
//import io.syspulse.haas.ingest.eth.alarm.UserAlarm
import io.syspulse.haas.ingest.eth.store.ScriptStore
import io.syspulse.haas.ingest.eth.script._

abstract class Interceptor[T](interceptions:Seq[Interception],scriptStore:ScriptStore,alarmThrottle:Long) {
  protected val log = Logger(s"${this.getClass()}")
  
  //scripts:Seq[String],alarmUri:Seq[String]

  import EthEtlJson._
  import DefaultJsonProtocol._

  // def reload(interceptions:Seq[Interception]) = {
  //   val scriptTriggers:Seq[ScriptTrigger] = interceptions.map(ix => {    
    
  //     val scriptId = s"${ix.id.toString}-${ix.name}"
  //     //Alarms.+(scriptId,ix.alarm)
      
  //     new ScriptTrigger(scriptId,ix.scriptUri,ix.id)
  //   })

  //   log.info(s"triggers=${scriptTriggers}")  
  //   scriptTriggers
  // }

  //val scriptTriggers:Seq[ScriptTrigger] = reload(interceptions)
  log.info(s"interceptions: ${interceptions}")

  val alarms = new Alarms(alarmThrottle)
  
  def decode(t:T):Map[String,Any]
 
  def scan(t:T):Seq[InterceptionAlarm] = {
    val txData = decode(t)

    val ii = interceptions.flatMap( ix => {
      //log.debug(s"${ix} => ${scriptStore.?(ix.scriptId)}")
      scriptStore.?(ix.scriptId) match {
        case Some(script) => {
          
          val engine = ScriptEngine.engines.get(script.typ)
          if(engine.isDefined) {
            val r = engine.get.run(script.src,txData)
            log.debug(s"${t}: ${script}: ${Console.YELLOW}${r}${Console.RESET}")
            
            if(! r.isDefined) 
              None
            else {
              val scriptOutput = s"${r.get}"                      
              Some(InterceptionAlarm(
                  System.currentTimeMillis(),
                  ix.id,
                  txData.get("block_number").getOrElse(0L).asInstanceOf[Long].toLong,
                  txData.get("transaction_hash").getOrElse("").asInstanceOf[String],
                  scriptOutput,
                  alarm = ix.alarm))
            }
          } else {
            log.error(s"engine not fonud: ${script.typ}")
            None
          }          
        }
        case None => None
      }            
    }).toSeq

    ii.foreach{ 
      ix => alarms.send(ix) 
    }
    ii
  }
}