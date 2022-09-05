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

case class ERC20Abi(addr:String,abi:Seq[AbiDefinition],funcTo:String, funcValue:String, name:String="") {
  override def toString = s"${getClass().getSimpleName()}(${addr},${abi.size},${name},${funcTo},${funcValue})"
}

class InterceptorERC20(config:Config) extends InterceptorTx(config) {
  
  log.info(s"scanning ABI: ${config.abi}")
  val abis = os.list(os.Path(config.abi,os.pwd)).flatMap( f => {        
    val (name,addr) = f.last.split("[-.]").toList match {
      case name :: addr :: _ => (name,addr.toLowerCase())
      case name :: Nil => (name,"")
      case _ => ("","")
    }

    if(!addr.isEmpty()) {
      val abi = Decoder.loadAbi(scala.io.Source.fromFile(f.toString).getLines().mkString("\n"))
      if(abi.size != 0) {
        log.info(s"${f}: ${abi.size}")

        val functionName = "transfer"
        // find function names
        val function = abi.filter(d => d.isFunction).find(_.name.get == functionName)
        if(function.isDefined) {

          val parToName = function.get.inputs.get(0).name
          val parValueName = function.get.inputs.get(1).name
          Some(ERC20Abi( addr, abi, parToName, parValueName, name ))

        } else {
          log.warn(s"${f}: could not find function: '${functionName}'")
          None
        }
      } else {
        log.warn(s"${f}: failed to load: ${abi}")
        None
      }
    }
    else {
      log.warn(s"${f}: could not determine addr")
      None
    }
  })

  // map of addr -> TokenAbi
  val erc20s = abis.map(ta => ta.addr -> ta).toMap
  log.info(s"ABI: ${erc20s}")

  override def parseTx(tx:Tx):Map[String,Any] = {
    val token = erc20s.get(tx.toAddress.getOrElse(""))

    val (toAddress,value,name) = if(token.isDefined) {    
      val funcHashSize = "0x12345678".size
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