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
import io.syspulse.haas.ingest.eth.EthEtlJson

import codegen.Decoder
import codegen.AbiDefinition

import io.syspulse.crypto.eth.abi._
import io.syspulse.haas.ingest.eth.store.ScriptStore
import io.syspulse.haas.ingest.eth.store.InterceptionStore

class InterceptorERC20(interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,abi:String,interceptions:Seq[Interception] = Seq()) 
  extends InterceptorTx(interceptionStore,scriptStore,alarmThrottle,interceptions) {
    
  val erc20 = AbiRepo.build().withRepo(new AbiRepoFiles(abi)).load()
  
  override def decode(tx:Tx):Map[String,Any] = {
    val token = erc20.findToken(tx.toAddress.getOrElse(""))

    val (fromAddress,toAddress,value,name) = if(token.isDefined) {    
      
      val di = erc20.decodeInput(tx.toAddress.get,tx.input)

      log.debug(s"${token}: ${tx.input}: ${di}")

      if(di.isSuccess) {
        val params = di.get.map(ntv => ntv._3)
                
        val (_from,_to,_value) = params.size match {
          case 2 => (tx.fromAddress,params(0),params(1))
          case 3 => (params(0),params(1),params(2))
          case _ => ("","","")
        }
        
        //val value = "%.2f".format(rawAmount.asInstanceOf[BigInt].toDouble / 10e6)
        //val value = "%.2f".format(rawAmount.asInstanceOf[BigInt].toDouble / 10e18)

        log.debug(s"ERC20: ${token}: ${_from},${_to} -> ${_value}")
        
        (_from,_to,_value,token.get.name)

      } else (tx.fromAddress,"","","")
    } else (tx.fromAddress,"","","")
    
    super.decode(tx) ++ Map( 
      ("token" -> name),
      ("from_address" -> fromAddress),
      ("to_address" -> toAddress),
      ("value" -> value),
    )
  }
 
}