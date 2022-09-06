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
import io.syspulse.haas.ingest.eth.script.Scripts
import io.syspulse.haas.ingest.eth.Config
import io.syspulse.haas.ingest.eth.EthJson

import codegen.Decoder
import codegen.AbiDefinition
import os._

class InterceptorERC20(config:Config) extends InterceptorTx(config) {
    
  val erc20s = AbiRepo.build().withRepo(new AbiRepoFiles(config.abi)).load()
  
  override def parseTx(tx:Tx):Map[String,Any] = {
    val token = erc20s.get(tx.toAddress.getOrElse(""))

    val (toAddress,value,name) = if(token.isDefined) {    
      val funcHashSize = AbiRepo.FUNC_HASH_SIZE
      val funcHash = tx.input.take(funcHashSize)
      val input = tx.input.drop(funcHashSize)
      
      val di = Decoder.decodeInput(token.get.abi,"transfer",input)

      log.debug(s"${token}: ${funcHash}: ${input}: ${di}")

      if(di.isSuccess) {
        val dmap = di.get.map(ntv => ntv._1 -> ntv._3).toMap
                
        val dst = dmap.get(token.get.funcTo).getOrElse("0x")
        val rawAmount = dmap.get(token.get.funcValue).getOrElse(0)
        
        //val value = "%.2f".format(rawAmount.asInstanceOf[BigInt].toDouble / 10e6)
        //val value = "%.2f".format(rawAmount.asInstanceOf[BigInt].toDouble / 10e18)
        val value = rawAmount

        log.debug(s"ERC20: ${token}: ${dst},${rawAmount} -> ${value}")
        
        (dst,value,token.get.name)

      } else ("","","")
    } else ("","","")
    
    super.parseTx(tx) ++ Map( 
      ("token" -> name),
      ("to_address" -> toAddress),
      ("value" -> value),
    )
  }
 
}