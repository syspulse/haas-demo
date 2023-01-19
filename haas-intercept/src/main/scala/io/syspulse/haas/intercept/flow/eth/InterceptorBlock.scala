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

import io.syspulse.haas.core.Block

import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception
import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore

class InterceptorBlock(interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[Block](interceptionStore,scriptStore,alarmThrottle,interceptions) {
  
  def entity():String = "block"
  override def decode(b:Block):Map[String,Any] = {
    Map( 
      ("block_number" -> b.number), //("number" -> b.number),
      ("hash" -> b.hash),
      ("parent_hash" -> b.parent_hash),
      ("nonce" -> b.nonce),
      ("sha3_uncles" -> b.sha3_uncles),
      ("logs_bloom" -> b.logs_bloom),
      ("transactions_root" -> b.transactions_root),
      ("state_root" -> b.state_root),
      ("receipts_root" -> b.receipts_root),

      ("miner" -> b.miner),
      ("difficulty" -> b.difficulty),
      ("total_difficulty" -> b.total_difficulty),

      ("size" -> b.size),
      ("extra_data" -> b.extra_data),
      ("gas_limit" -> b.gas_limit),
      ("gas_used" -> b.gas_used),

      ("timestamp" -> b.timestamp),
      ("transaction_count" -> b.transaction_count),
      ("base_fee_per_gas" -> b.base_fee_per_gas),      
    )
  }
 
}