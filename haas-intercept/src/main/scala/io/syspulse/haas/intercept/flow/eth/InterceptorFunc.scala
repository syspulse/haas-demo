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

class InterceptorFunc(abiStore:AbiStore,interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[Tx](interceptionStore,scriptStore,alarmThrottle,interceptions) {
    
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
    if(tx.input == "" || tx.input == "0x" || ! tx.toAddress.isDefined)
      return Map()

    Map( 
      ("from_address" -> tx.fromAddress),
      ("contract" -> tx.toAddress.get),
      ("value" -> tx.value),
      ("gas" -> tx.value),
      ("input" -> tx.input),
      ("block_number" -> tx.blockNumber),
      ("hash" -> tx.hash), //("transaction_hash" -> tx.hash),      
      ("ts" -> tx.ts),
    ) ++       
      decodeData(tx.toAddress.get,tx.input).getOrElse(Map())      
  }
}