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
import io.syspulse.haas.ingest.eth.store.ScriptStore
import io.syspulse.haas.ingest.eth.store.InterceptionStore

class InterceptorTx(interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[Tx](interceptionStore,scriptStore,alarmThrottle,interceptions) {
  
  override def decode(tx:Tx):Map[String,Any] = {
    Map( 
      ("from_address" -> tx.fromAddress),
      ("to_address" -> tx.toAddress.getOrElse("null")),
      ("value" -> tx.value),
      ("gas" -> tx.value),
      ("input" -> tx.input),
      ("block_number" -> tx.blockNumber),
      ("transaction_hash" -> tx.hash),
    )
  }
 
}