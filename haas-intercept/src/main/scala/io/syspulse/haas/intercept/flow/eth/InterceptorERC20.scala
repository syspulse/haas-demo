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

import io.syspulse.skel.dsl.JS
import io.syspulse.haas.core.Tx
import io.syspulse.haas.ingest.eth.EthEtlJson

import codegen.Decoder
import codegen.AbiDefinition

import io.syspulse.skel.crypto.eth.abi._

import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception

import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore
import io.syspulse.skel.crypto.eth.abi.AbiStoreRepo
import io.syspulse.skel.crypto.eth.abi.AbiStore

class InterceptorERC20(abiStore:AbiStore,interceptionStore:InterceptionStore,scriptStore:ScriptStore,alarmThrottle:Long,interceptions:Seq[Interception] = Seq()) 
  extends InterceptorTx(interceptionStore,scriptStore,alarmThrottle,interceptions) {

  override def entity():String = "erc20"

  val erc20 = abiStore //AbiStoreRepo.build().withRepo(new AbiStoreDir(dir).load().get
    //AbiRepo.build().withRepo(new AbiRepoFiles(abi)).load()

  val tokenLabels = Map(
    "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984" -> "UNI",
    "0x6123b0049f904d730db3c36a31167d9d4121fa6b" -> "RBN",
    "0xdAC17F958D2ee523a2206206994597C13D831ec7" -> "USDT",
    "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48" -> "USDC",
  )
  
  override def decode(tx:Tx):Map[String,Any] = {
    // find token name from Labels
    val token = tokenLabels
      .find{ case(a,t) => tx.to.map(_.toLowerCase == t.toLowerCase()).getOrElse(false)}
      .map(_._2)

    val (fromAddress,toAddress,value,name) = 
    {    
      
      val di = erc20.decodeInput(tx.to.get,Seq(tx.inp),"transfer")

      log.debug(s"${token}: ${tx.inp}: ${di}")

      if(di.isSuccess) {
        val params = di.get.params.map(ntv => ntv._3)
                
        val (_from,_to,_value) = params.size match {
          case 2 => (tx.from,params(0),params(1))
          case 3 => (params(0),params(1),params(2))
          case _ => ("","","")
        }
        
        //val value = "%.2f".format(rawAmount.asInstanceOf[BigInt].toDouble / 10e6)
        //val value = "%.2f".format(rawAmount.asInstanceOf[BigInt].toDouble / 10e18)

        log.debug(s"ERC20: ${token}: ${_from},${_to} -> ${_value}")
        
        (_from,_to,_value,token.get)

      } else (tx.from,"","","")
    } 
    
    super.decode(tx) ++ Map( 
      ("token" -> name),
      ("from_address" -> fromAddress),
      ("to_address" -> toAddress),
      ("value" -> value),
    )
  }
 
}