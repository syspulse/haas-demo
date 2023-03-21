package io.syspulse.haas.intercept.flow.eth

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

import io.syspulse.haas.core.TokenTransfer

import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception

import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore
import io.syspulse.haas.core.Blockchain

class InterceptorTokenTransfer(bid:Blockchain.ID,interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[TokenTransfer](bid,interceptionStore,scriptStore,alarmThrottle,interceptions) {
  
  def entity():String = "token"
  override def decode(t:TokenTransfer):Map[String,Any] = {
    Map(
      ("token_address" -> t.contract), 
      ("from_address" -> t.from),
      ("to_address" -> t.to),
      ("value" -> t.value),
      ("block_number" -> t.block),
      ("hash" -> t.hash), //("transaction_hash" -> t.hash),
      ("index" -> t.i), //("transaction_hash" -> t.hash),
      ("ts" -> t.ts),
    )
  }
 
}