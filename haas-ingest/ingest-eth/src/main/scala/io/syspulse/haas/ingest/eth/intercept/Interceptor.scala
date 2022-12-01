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


abstract class Interceptor[T](scripts:Seq[String],alarmUri:Seq[String],alarmThrottle:Long) {
  protected val log = Logger(s"${this.getClass()}")
  
  import EthEtlJson._
  import DefaultJsonProtocol._

  val scriptTriggers:Seq[ScriptTrigger] = scripts.map(s => {    
    val scriptId = s"SCRIPT-${s}"
    
    //Alarms.+(UserAlarm(scriptId,alarmUri))
    Alarms.+(scriptId,alarmUri)
    
    new ScriptTrigger(scriptId,s)
  })

  log.info(s"triggers=${scriptTriggers}")
  
  val alarms = new Alarms(alarmThrottle)
  log.info(s"alarms: ${Alarms.userAlarms}")

  def decode(t:T):Map[String,Any]
 
  def scan(t:T):Seq[Interception] = {
    val txData = decode(t)

    val ii = scriptTriggers.flatMap( st => {
      st.getScript() match {
        case Some(script) => {
          val r = script.run(txData)
          log.debug(s"${t}: ${script}: ${Console.YELLOW}${r}${Console.RESET}")
          
          if(! r.isDefined) 
            None
          else {
            val scriptOutput = s"${r.get}"                      
            Some(Interception(System.currentTimeMillis(),st.getScriptId(),
                 txData.get("block_number").getOrElse(0L).asInstanceOf[Long].toLong,
                 txData.get("transaction_hash").getOrElse("").asInstanceOf[String],
                 scriptOutput))
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