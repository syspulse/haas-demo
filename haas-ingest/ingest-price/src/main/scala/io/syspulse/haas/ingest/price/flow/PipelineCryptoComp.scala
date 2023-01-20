package io.syspulse.haas.ingest.price.flow

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
import java.util.concurrent.TimeUnit

import io.syspulse.haas.core.Price
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.ingest.price.CryptoCompJson
import io.syspulse.haas.ingest.price._

import io.syspulse.haas.serde.PriceJson._
import io.syspulse.haas.ingest.price.PriceURI
import akka.stream.scaladsl.Framing
import io.syspulse.haas.serde.PriceDecoder

class PipelineCryptoComp(feed:String,output:String)(implicit config:Config) extends PipelinePrice[CryptoComp](feed,output) {

  import CryptoCompJson._

  val sourceID = DataSource.id("cryptocomp")

  val TOKENS_SLOT = "COINS"

  override def apiSuffix():String = s"?fsyms=${TOKENS_SLOT}&tsyms=${config.tokensPair.mkString(",")}"
  override def processing:Flow[CryptoComp,CryptoComp,_] = Flow[CryptoComp].map(v => v)

  def parseCryptoComp(data:String):Seq[CryptoComp] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        val price = data.parseJson.convertTo[CryptoComp]
        //log.info(s"price=${price}")
        Seq(price)
      } else {
        // assume csv
        val price = data.split(",").toList match {
          case id :: ts :: v :: Nil => 
            Some(CryptoComp(`RAW` = Map(id -> Map("USD" -> CryptoCompData(id,v.toDouble,ts.toLong)))))
          case _ => {
            log.error(s"failed to parse: '${data}'")
            None
          }
        }
        //log.info(s"price=${price}")
        price.toSeq
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def parse(data:String):Seq[CryptoComp] = {
    log.debug(s"data=${data}")
    if(data.isEmpty()) return Seq()

    parseCryptoComp(data)
  }

  def transform(p: CryptoComp): Seq[Price] = {
    p.`RAW`.map{ case(token,pair) =>
      pair.map{ case(p,info) => 
        config.priceFormat match {
          case "price" => Price(token, info.`LASTUPDATE` * 1000L, info.`PRICE`,pair = Some(p), src = sourceID)
          case "telemetry" => Price(s"${token}-${p}", info.`LASTUPDATE` * 1000L, info.`PRICE`,None, src = sourceID)
        }
        
      }      
    }.flatten.toSeq
  }

  override def source() = {
    PriceURI(feed,apiSuffix()).parse() match {
      case Some(uri) => source(uri)
      case None => super.source()
    }
  }

  def fromHttpListAsFlow(req: Seq[HttpRequest],par:Int = 1, frameDelimiter:String="\n",frameSize:Int = 8192,throttle:Long = 10L)(implicit as:ActorSystem) = {
    val f1 = Flow[String]
      .throttle(1,FiniteDuration(throttle,TimeUnit.MILLISECONDS))
      .mapConcat(tick => {        
        req
      })
      .mapAsync(par)(r => {
        log.info(s"--> ${req}")
        Flows.fromHttpFuture(r)(as)
      })      
    
    if(frameDelimiter.isEmpty())
      f1
    else
      f1.via(Framing.delimiter(ByteString(frameDelimiter), maximumFrameLength = frameSize, allowTruncation = true))
  }

  override def source(feed:String) = {
    feed.split("://").toList match {
      case "http" :: _ | "https" :: _ => {
        // val reqs = tokensFilter.map( t => 
        //   HttpRequest(uri = feed.replaceFirst(TOKEN_SLOT,t)).withHeaders(Accept(MediaTypes.`application/json`))
        // )
        val reqs = Seq(
          HttpRequest(uri = feed.replaceFirst(TOKENS_SLOT,tokensFilter.mkString(",")))
            .withHeaders(Accept(MediaTypes.`application/json`))
        )
        log.info(s"reqs=${reqs}")

        val s = Source.tick(FiniteDuration(10,TimeUnit.MILLISECONDS), 
                            FiniteDuration(config.ingestCron.toLong,TimeUnit.SECONDS), 
                            s"ingest-${System.currentTimeMillis()}-${feed}")
        .map(h => {
          log.info(s"Cron --> ${h}")
          h
        })
        .via(
          fromHttpListAsFlow(reqs, par = 1, frameDelimiter = config.delimiter,frameSize = config.buffer, throttle = config.throttleSource)
        )
        s
      }
            
      case _ => super.source(feed)
    }
  }
}
