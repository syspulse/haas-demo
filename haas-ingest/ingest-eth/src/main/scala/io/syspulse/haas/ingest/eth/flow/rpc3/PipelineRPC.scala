package io.syspulse.haas.ingest.eth.flow.rpc3

import java.util.concurrent.atomic.AtomicLong
import io.syspulse.skel.ingest.flow.Flows

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import akka.util.ByteString
import akka.http.scaladsl.model.{HttpRequest,HttpMethods,HttpEntity,ContentTypes}
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter

import io.syspulse.skel
import io.syspulse.skel.config._
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.skel.ingest._
import io.syspulse.skel.ingest.store._
import io.syspulse.skel.ingest.flow.Pipeline

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.skel.serde.Parq._
import com.github.mjakubowski84.parquet4s.{ParquetRecordEncoder,ParquetSchemaResolver}

import java.util.concurrent.TimeUnit

import io.syspulse.haas.core.{ Block, Tx, TokenTransfer, Event }
import io.syspulse.haas.ingest.eth.rpc3._
import io.syspulse.haas.ingest.eth.rpc3.EthRpcJson._

import io.syspulse.haas.ingest.eth.EthURI
import io.syspulse.haas.ingest.eth.flow.PipelineEth
import io.syspulse.haas.ingest.eth

import io.syspulse.haas.ingest.eth.Config
import akka.actor.typed.ActorSystem
import akka.stream.RestartSettings
import scala.util.control.NoStackTrace

class RetryException(msg: String) extends RuntimeException(msg) with NoStackTrace
class BehindException(behind: Long) extends RuntimeException("") with NoStackTrace

// ATTENTION !!!
// throttle is overriden in Config to support batchable retries !
abstract class PipelineRPC[T,O <: skel.Ingestable,E <: skel.Ingestable](config:Config)
                                                                       (implicit fmt:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E])
  extends PipelineEth[T,O,E](config.copy(throttle = 0L))(fmt,parqEncoders,parsResolver) with RPCDecoder[E] {

  override val retrySettings:Option[RestartSettings] = Some(RestartSettings(
    minBackoff = FiniteDuration(1000,TimeUnit.MILLISECONDS),
    maxBackoff = FiniteDuration(1000,TimeUnit.MILLISECONDS),
    randomFactor = 0.2
  ))

  import EthRpcJson._

  val cursor = new CursorBlock()
    
  override def source(feed:String) = {
    feed.split("://").toList match {
      case "http" :: _ | "https" :: _ => 
        
        val blockStr = 
          (config.block.split("://").toList match {
            case "file" :: file :: Nil => cursor.read(file)
            case "file" :: Nil => cursor.read()
            case _ => config.block
          })

        val blockStart = blockStr.strip match {
          case "latest" =>
            val rsp = requests.post(feed,
              headers = Seq(("Content-Type","application/json")),
              data = s"""{
                "jsonrpc":"2.0","method":"eth_getBlockByNumber",
                "params":["latest",false],
                "id":0
              }""".trim.replaceAll("\\s+","")
            )
            if(rsp.statusCode != 200) {
              log.error(s"failed to get latest block: ${rsp}")
              0
            } else {
              val latest = ujson.read(rsp.text()).obj("result").obj("number").str
              java.lang.Long.parseLong(latest.stripPrefix("0x"),16).toInt
            }
          case hex if hex.startsWith("0x") =>
            java.lang.Long.parseLong(hex,16).toInt
          case dec =>
            dec.toInt
        }
        
        val blockEnd = config.blockEnd match {
          case "" => Int.MaxValue
          case "latest" => blockStart
          case hex if hex.startsWith("0x") =>
            java.lang.Long.parseLong(hex,16).toInt
          case _ @ dec =>
            dec.toInt
        }

        cursor.init(blockStart, blockEnd)
                   
        log.info(s"cursor: ${cursor}")        

        val sourceTick = Source.tick(
          FiniteDuration(10,TimeUnit.MILLISECONDS), 
          //FiniteDuration(config.ingestCron.toLong,TimeUnit.SECONDS),
          FiniteDuration(config.throttle,TimeUnit.MILLISECONDS),
          s"ingest-eth-${feed}"
        )

        sourceTick
          .map(h => {
            log.info(s"Cron --> ${h}")

            // request latest block to know where we are from current
            val blockHex = "latest"
            val json = s"""{
                "jsonrpc":"2.0","method":"eth_blockNumber",
                "params":[],
                "id": 0
              }""".trim.replaceAll("\\s+","")

            val rsp = requests.post(config.feed, data = json,headers = Map("content-type" -> "application/json"))
            //log.info(s"rsp=${rsp.statusCode}: ${rsp.text()}")
            val r = ujson.read(rsp.text())
            val lastBlock = java.lang.Long.decode(r.obj("result").str)
            
            log.info(s"last=${lastBlock}, current=${cursor.get()}")
            lastBlock
          })
          .mapConcat(lastBlock => {
            cursor.current to lastBlock            
          })
          //.throttle(1,FiniteDuration(config.throttle,TimeUnit.MILLISECONDS))
          .map(block => {
            log.info(s"--> ${block}")
            
            val blockHex = s"0x${block.toHexString}"
            val json = s"""{
                "jsonrpc":"2.0","method":"eth_getBlockByNumber",
                "params":["${blockHex}",true],
                "id":0
              }""".trim.replaceAll("\\s+","")

            val rsp = requests.post(config.feed, data = json,headers = Map("content-type" -> "application/json"))
            log.info(s"rsp=${rsp.statusCode}")
            
            ByteString(rsp.text())
          })                          
      
      case _ => super.source(feed)
    }
  }
}
