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

import io.syspulse.haas.core.Tx

import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception
import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore
import io.syspulse.skel.crypto.eth.abi.AbiStore
import scala.util.Failure
import io.syspulse.haas.core.Blockchain

class InterceptorFunc(bid:Blockchain.ID,abiStore:AbiStore,interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[Tx](bid,interceptionStore,scriptStore,alarmThrottle,interceptions) {
    
  def entity():String = "function"

  def decodeData(addr:String,data:String):Option[Map[String,Any]] = {

    abiStore.decodeInput(addr,Seq(data),AbiStore.FUNCTION) match {
      case Success(r) => 
        val m = 
          r.params.map{ case(name,typ,v) => s"func_param_${name}" -> v}.toMap ++
          r.params.zipWithIndex.map{ case((name,typ,v),i) => s"func_param_${i}" -> v}.toMap +
          ("func_sig" -> r.sig)
        
        Some(m)
      case Failure(e) => 
        //log.warn(s"${addr}: failed to decode ABI: ${data}: ${e.getMessage()}")
        Some(Map("func_sig" -> ""))
    }    
  }
  
  override def decode(tx:Tx):Map[String,Any] = {
    // cannot process, because this is not a contract call
    if(tx.inp == "" || tx.inp == "0x" || ! tx.to.isDefined)
      return Map()

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
      ("type" -> tx.typ),
      ("gas_used_cumulative" -> tx.used2),
      ("gas_used" -> tx.used),
      ("contract" -> tx.to.getOrElse("null")),
      ("receipt_root" -> tx.root.getOrElse("null")),
      ("status" -> tx.sta),
      ("price_effective" -> tx.p0),

    ) ++       
      decodeData(tx.to.get,tx.inp).getOrElse(Map())      
  }
}