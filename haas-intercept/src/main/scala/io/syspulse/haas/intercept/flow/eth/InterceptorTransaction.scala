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

import io.syspulse.haas.core.Transaction

import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception
import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore
import io.syspulse.haas.core.Blockchain

class InterceptorTransaction(bid:Blockchain.ID,interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[Transaction](bid,interceptionStore,scriptStore,alarmThrottle,interceptions) {
    
  def entity():String = "transaction"
  
  override def decode(tx:Transaction):Map[String,Any] = {
    Map( 
      ("from_address" -> tx.from),
      ("to_address" -> tx.to.getOrElse("null")),
      ("value" -> tx.v),
      ("gas" -> tx.gas),
      ("price" -> tx.p),
      ("input" -> tx.inp),
      ("block_number" -> tx.blk),
      ("hash" -> tx.hash), //("transaction_hash" -> tx.hash),      
      ("ts" -> tx.ts),

      ("nonce" -> tx.non),
      ("max_fee" -> tx.fee.getOrElse("null")),
      ("max_tip" -> tx.tip.getOrElse("null")),
      ("type" -> tx.typ.getOrElse("null")),
      ("gas_used_cumulative" -> tx.used2),
      ("gas_used" -> tx.used),
      ("contract" -> tx.con.getOrElse("null")),
      ("receipt_root" -> tx.root.getOrElse("null")),
      ("status" -> tx.sta),
      ("price_effective" -> tx.p0.getOrElse("null")),

    )
  }
 
}