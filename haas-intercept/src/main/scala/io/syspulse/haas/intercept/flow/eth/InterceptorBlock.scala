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
import io.syspulse.haas.core.Blockchain

class InterceptorBlock(bid:Blockchain.ID,interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[Block](bid,interceptionStore,scriptStore,alarmThrottle,interceptions) {
  
  def entity():String = "block"
  override def decode(b:Block):Map[String,Any] = {
    Map( 
      ("block_number" -> b.i), //("number" -> b.number),
      ("hash" -> b.hash),
      ("parent_hash" -> b.phash),
      ("nonce" -> b.non),
      ("sha3_uncles" -> b.uncl),
      ("logs_bloom" -> b.bloom),
      ("transactions_root" -> b.txrt),
      ("state_root" -> b.strt),
      ("receipts_root" -> b.rert),

      ("miner" -> b.miner),
      ("difficulty" -> b.dif),
      ("total_difficulty" -> b.dif0),

      ("size" -> b.sz),
      ("extra_data" -> b.data),
      ("gas_limit" -> b.limit),
      ("gas_used" -> b.used),

      ("timestamp" -> b.ts),
      ("transaction_count" -> b.cnt),
      ("base_fee_per_gas" -> b.fee),      
    )
  }
 
}