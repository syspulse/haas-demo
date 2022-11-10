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
import io.syspulse.haas.ingest.eth.script.ScriptTrigger
import io.syspulse.haas.ingest.eth.alarm.Alarms
import io.syspulse.haas.ingest.eth.alarm.UserAlarm


abstract class Interceptor(scripts:Seq[String],alarmsUri:Seq[String],alarmThrottle:Long) {
  protected val log = Logger(s"${this.getClass()}")
  
  import EthEtlJson._
  import DefaultJsonProtocol._

  val scriptTriggers:Seq[ScriptTrigger] = scripts.map(s => {
    val scriptId = "script-"+Math.abs(s.hashCode).toString
    Alarms.+(UserAlarm(scriptId,alarmsUri))
    new ScriptTrigger(scriptId,s)
  })
  
  val alarms = new Alarms(alarmThrottle)
  
  log.info(s"triggers=${scriptTriggers}")

  def parseTx(tx:Tx):Map[String,Any]
 
  def scan(tx:Tx):Seq[Interception] = {
    val txData = parseTx(tx)

    val ii = scriptTriggers.flatMap( st => {
      st.getScript() match {
        case Some(script) => {
          val r = script.run(txData)
          log.debug(s"tx: ${tx}: ${script}: ${Console.YELLOW}${r}${Console.RESET}")
          
          if(! r.isDefined) 
            None
          else {
            val scriptOutput = s"${r.get}"                      
            Some(Interception(System.currentTimeMillis(),st.getScriptId(),tx.blockNumber,tx.hash,scriptOutput))
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