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

import io.syspulse.skel.dsl.JS
import io.syspulse.haas.core.Tx
import io.syspulse.haas.ingest.eth.script.Scripts
import io.syspulse.haas.ingest.eth.Config
import io.syspulse.haas.ingest.eth.EthJson

class InterceptorTx(config:Config) {
  private val log = Logger(s"${this.getClass()}")
  
  import EthJson._
  import DefaultJsonProtocol._

  Scripts.+(config.script)
  Console.err.println(Scripts.scripts)
 
  def scan(tx:Tx):Seq[Interception] = {
    Scripts.scripts.flatMap {
      case(userScript,userAlarms) => {
        val js = userScript.javascript()
        
        // JavaScript returns null !
        val r = Option(
          if(js.isSuccess) js.get.run(
            Map( 
              ("from_address" -> tx.fromAddress),
              ("to_address" -> tx.toAddress.getOrElse("null")),
              ("value" -> tx.value),
              ("gas" -> tx.value),
              ("input" -> tx.input),
            )
         ) else null
        )

        log.debug(s"tx: ${tx}: ${userScript}: ${Console.YELLOW}${r}${Console.RESET}")

        if(! r.isDefined) None
        else {
          val scriptOutput = s"${r.get}"
          
          userAlarms.filter(_.to.isEnabled).map{ ua => 
            ua.to.send(scriptOutput)
          }
          
          Some(Interception(tx.blockNumber,tx.hash,scriptOutput))
        }
      }
    }.toSeq
    
  }
}