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

import scala.util.{Try,Success,Failure}

import io.syspulse.haas.core.Event

import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception

import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore
import io.syspulse.skel.crypto.eth.abi.SignatureStore
import io.syspulse.skel.crypto.eth.abi.EventSignature
import io.syspulse.skel.crypto.eth.abi.AbiStore

class InterceptorEvent(abiStore:AbiStore,interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends Interceptor[Event](interceptionStore,scriptStore,alarmThrottle,interceptions) {
  
  def entity():String = "event"

  def decodeData(addr:String,data:String,topics:List[String]):Option[Map[String,Any]] = {

    abiStore.decodeInput(addr,topics :+ data,"event") match {
      case Success(r) => 
        val sig = r.toString
        val m = r.params.map{ case(name,k,v) => name -> v}.toMap ++ 
          Map("event_name" -> r.name, "event_sig" -> sig)
        Some(m)
      case Failure(e) => 
        log.warn(s"${addr}: failed to decode ABI: ${topics}: ${e.getMessage()}")
        Some(Map("event_name" -> "", "event_sig" -> ""))
    }    
  }

  override def decode(t:Event):Map[String,Any] = {
    val m = Map(
      ("contract" -> t.contract), 
      ("data" -> t.data),
      ("block_number" -> t.block),
      ("hash" -> t.hash), //("transaction_hash" -> t.hash),      
      ("ts" -> t.ts),
    ) ++ 
      decodeData(t.contract,t.data,t.topics).getOrElse(Map())

    log.debug(s"script.map=${m}")
    m
  }
 
}