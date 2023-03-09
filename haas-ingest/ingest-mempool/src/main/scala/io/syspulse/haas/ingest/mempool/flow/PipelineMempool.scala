package io.syspulse.haas.ingest.mempool

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import akka.actor.ActorSystem
import akka.util.ByteString
import akka.http.javadsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Framing

import io.syspulse.skel
import io.syspulse.skel.config._
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.skel.ingest._
import io.syspulse.skel.ingest.store._
import io.syspulse.skel.ingest.flow.Pipeline
import io.syspulse.skel.ingest.flow.Flows

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.skel.serde.Parq._

import java.util.concurrent.TimeUnit

import io.syspulse.haas.evm.EvmTxPoolJson._
import io.syspulse.haas.evm.EvmTx
import io.syspulse.haas.serde.MempoolJson._

import io.syspulse.haas.core.MempoolTx

class PipelineMempool(feed:String,output:String)(implicit config:Config) extends Pipeline[EvmTx,EvmTx,MempoolTx](feed,output) {
  protected val log = Logger(s"${this}")  
  val sourceID = 0 // internal
  
  def toBigInt(v:String):Option[BigInt] = try {
    Some(BigInt(v))
  } catch {
    case _ : Exception => None
  }

  def decodeMempool(data:String):Seq[MempoolTx] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        try {
          val m = data.parseJson.convertTo[MempoolTx]
          Seq(m)
        } catch {
          case e:Exception => 
            log.error(s"failed to parse: '${data}'",e)
            Seq()
        }
      } else {
        data.split(",").toList match {
          case ts :: pool :: from :: gas :: p :: fee :: tip :: hash :: inp :: non :: to :: v :: typ :: sig :: Nil =>
            Seq(MempoolTx(
              ts.toLong,
              pool.toInt,
              from,
              gas.toLong,
              BigInt(p),
              toBigInt(fee),
              toBigInt(tip),
              hash,
              inp,
              non.toLong,
              Option(to),
              BigInt(v),
              typ.toInt,
              sig
            ))
          case _ => {
            log.error(s"failed to parse: '${data}'")
            Seq()
          }
        }
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }    
  }

  def parse(data:String):Seq[EvmTx] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        try {
          val m = data.parseJson.convertTo[EvmTx]
          Seq(m)
        } catch {
          case e:Exception => 
            log.error(s"failed to parse: '${data}'",e)
            Seq()
        }
      } else {
        log.error(s"only json supported: '${data}'")
        Seq()
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }    
  }

  def process:Flow[EvmTx,EvmTx,_] = Flow[EvmTx].map(v => v)
  
  def transform(etx: EvmTx): Seq[MempoolTx] = 
    Seq(MempoolTx(
      ts = etx.ts,
      pool = (if(etx.pool == "pending") 0 else 1),
      from = etx.from,
      gas = etx.gas,
      p = etx.gasPrice,
      fee = etx.maxFeePerGas,
      tip = etx.maxPriorityFeePerGas,
      hash = etx.hash,
      inp = etx.input,
      non = etx.nonce,
      to = etx.to,
      v = etx.value,
      typ = etx.`type`,
      sig = s"${etx.r}:${etx.s}:${etx.v}"
    ))
}
