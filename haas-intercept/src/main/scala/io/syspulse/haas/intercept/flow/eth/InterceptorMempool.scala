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

import io.syspulse.skel.service.JsonCommon
import spray.json._
import spray.json.{DefaultJsonProtocol,NullOptions}

import java.time.LocalDateTime
import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success

import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception
import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore
import io.syspulse.haas.core.Blockchain
import io.syspulse.haas.core.MempoolTx

class InterceptorMempool(bid:Blockchain.ID,interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[MempoolTx](bid,interceptionStore,scriptStore,alarmThrottle,interceptions) {
    
  def entity():String = "mempool"
  
  override def decode(tx:MempoolTx):Map[String,Any] = {
    Map( 
      ("from_address" -> tx.from),
      ("to_address" -> tx.to.getOrElse("null")),
      ("value" -> tx.v),
      ("gas" -> tx.gas),
      ("price" -> tx.p),
      ("input" -> tx.inp),
      ("hash" -> tx.hash), //("transaction_hash" -> tx.hash),      
      ("ts" -> tx.ts),

      ("nonce" -> tx.non),
      ("max_fee" -> tx.fee.getOrElse("null")),
      ("max_tip" -> tx.tip.getOrElse("null")),
      ("type" -> tx.typ),
      ("sig" -> tx.sig)
    )
  }
 
}