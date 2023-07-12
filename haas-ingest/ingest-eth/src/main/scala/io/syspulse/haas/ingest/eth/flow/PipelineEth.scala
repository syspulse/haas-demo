package io.syspulse.haas.ingest.eth.flow

import java.util.concurrent.atomic.AtomicLong
import io.syspulse.skel.ingest.flow.Flows

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import akka.util.ByteString
import akka.http.javadsl.Http
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
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._

import io.syspulse.haas.ingest.eth.EthURI

case class LastBlock(block:Long, blockStart:Long, blockEnd:Long, stateStore:Option[String])

object LastBlock {
  private val log = Logger(s"LastBlock")

  @volatile
  private var lastBlock: Option[LastBlock] = None

  override def toString() = s"${lastBlock}"

  def commit(block:Long) = {
    lastBlock.synchronized {
      log.info(s"COMMIT: ${block}")
      lastBlock = lastBlock.map(lb => lb.copy(block = block + 1))
    }
  }

  def isDefined = lastBlock.isDefined

  def set(block:Long,blockStart:Long,blockEnd:Long,stateFile:Option[String]) = {
    lastBlock = lastBlock.synchronized {
      lastBlock match {
        case Some(_) => lastBlock
        case None => Some(LastBlock(block,blockStart,blockEnd,stateFile))    
      }
    }    
  }

  def current() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => lb.block
      case None => -1
    }
  }

  def last() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => lb.blockEnd
      case None => -1
    }
  }
}

abstract class PipelineEth[T,O <: skel.Ingestable,E <: skel.Ingestable](config:Config)
                                                                       (implicit val fmt:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E])
  extends Pipeline[T,O,E](config.feed,config.output,config.throttle,config.delimiter,config.buffer) {
  
  private val log = Logger(s"${this}")
  
  var latestTs:AtomicLong = new AtomicLong(0)
    

  override def getRotator():Flows.Rotator = 
    new Flows.RotatorTimestamp(() => {
      latestTs.get()
    })

  override def getFileLimit():Long = config.limit
  override def getFileSize():Long = config.size

  def filter():Seq[String] = config.filter
  def apiSuffix():String

  def convert(t:T):O

  //def transform(o: O) = Seq(o)

  def process:Flow[T,O,_] = Flow[T].map(t => {
    val o = convert(t)
    //log.debug(s"${o}")
    o
  })

  override def source(feed:String) = {
    feed.split("://").toList match {
      case "node" :: _ => super.source(EthURI(feed,apiSuffix()).uri)

      case "http" :: _ | "https" :: _ => {
        val lastBlock = if( ! LastBlock.isDefined) {
          val (blockStr,stateFile) = 
            (config.block.split("://").toList match {
              case "file" :: file :: Nil => // state file
                (os.read(os.Path(file,os.pwd)),Some(file))
              case _ => 
                (config.block,None)
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

          LastBlock.set(blockStart,blockStart,blockEnd,stateFile)
          
        } else {
          
        }

        log.info(s"blocks: ${LastBlock}")

        lazy val reqs = LazyList.from(LastBlock.current().toInt,1).takeWhile(_ <= LastBlock.last()).map { block => 
          val id = System.currentTimeMillis() / 1000L
          val blockHex = "0x%x".format(block)
          val json = s"""{
                "jsonrpc":"2.0","method":"eth_getBlockByNumber",
                "params":["${blockHex}",true],
                "id":${block}
              }""".trim.replaceAll("\\s+","")
          
          log.info(s"block=${block}: req='${json}'")
            
          HttpRequest( method = HttpMethods.POST, uri = feed,
              entity = HttpEntity(ContentTypes.`application/json`,json)
          ).withHeaders(Accept(MediaTypes.`application/json`))
        }
            
        //log.info(s"reqs=${reqs}")

        val sourceHttp = Source.tick(FiniteDuration(10,TimeUnit.MILLISECONDS), 
                            FiniteDuration(config.ingestCron.toLong,TimeUnit.SECONDS),
                            s"ingest-eth-${feed}")
        .map(h => {
          log.info(s"Cron --> ${h}")
          h
        })
        .via(
          Flows.fromHttpListAsFlow(reqs, 
            par = 1, 
            frameDelimiter = config.delimiter,
            frameSize = config.buffer, 
            throttle = config.throttleSource)
        )
        sourceHttp
      }
            
      case _ => super.source(feed)
    }
  }

  // override def processing:Flow[T,T,_] = Flow[T].map(v => {
  //   if(countObj % reportFreq == 0)
  //     log.info(s"processed: ${countInput},${countObj}")
  //   v
  // })

}
