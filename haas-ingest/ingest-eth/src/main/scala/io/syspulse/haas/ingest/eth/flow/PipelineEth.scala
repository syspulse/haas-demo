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
        val block = "latest"

        val reqs = {
          val id = System.currentTimeMillis() / 1000L
          val json = s"""{
                "jsonrpc":"2.0","method":"eth_getBlockByNumber",
                "params":["${block}",true],
                "id":${id}
              }""".trim.replaceAll("\\s+","")
          
          log.info(s"req='${json}'")
            
          HttpRequest( method = HttpMethods.POST, uri = feed,
              entity = HttpEntity(ContentTypes.`application/json`,json)
          ).withHeaders(Accept(MediaTypes.`application/json`))
        }
            
        log.info(s"reqs=${reqs}")

        val sourceHttp = Source.tick(FiniteDuration(10,TimeUnit.MILLISECONDS), 
                            FiniteDuration(config.ingestCron.toLong,TimeUnit.SECONDS),
                            s"ingest-eth-${feed}")
        .map(h => {
          log.info(s"Cron --> ${h}")
          h
        })
        .via(
          Flows.fromHttpListAsFlow(Seq(reqs), 
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
