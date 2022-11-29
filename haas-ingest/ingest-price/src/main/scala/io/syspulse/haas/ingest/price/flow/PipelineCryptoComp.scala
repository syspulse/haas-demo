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
import io.syspulse.haas.ingest.price.CryptoCompJson
import io.syspulse.haas.ingest.price._

import io.syspulse.haas.core.serde.PriceJson._
import io.syspulse.haas.ingest.price.PriceURI
import akka.stream.scaladsl.Framing

class PipelineCryptoComp(feed:String,output:String)(implicit config:Config) extends PipelinePrice[CryptoComp](feed,output) {

  import CryptoCompJson._

  val TOKENS_SLOT = "COINS"

  override def apiSuffix():String = s"?fsyms=${TOKENS_SLOT}&tsyms=USD"
  override def processing:Flow[CryptoComp,CryptoComp,_] = Flow[CryptoComp].map(v => v)

  def parse(data:String):Seq[CryptoComp] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        val price = data.parseJson.convertTo[CryptoComp]
        log.info(s"price=${price}")
        Seq(price)
      } else {
        // assume csv
        val price = data.split(",").toList match {
          case id :: ts :: v :: Nil => 
            Some(CryptoComp(`RAW` = Map(id -> CryptoCompUSD(`USD` = CryptoCompData(id,v.toDouble,ts.toLong)))))
          case _ => {
            log.error(s"failed to parse: '${data}'")
            None
          }
        }
        price.toSeq
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def transform(p: CryptoComp): Seq[Price] = {
    p.`RAW`.map{ case(token,info) =>
      Price(token,info.`USD`.`LASTUPDATE`, info.`USD`.`PRICE`)
    }.toSeq
  }

  override def source() = {
    // feed.split("://").toList match {
    //   case "cryptocomp" :: _ => source(PriceURI(feed,apiSuffix()).uri) 
    //   case "http" :: _ | "https" :: _ => source(PriceURI(feed,apiSuffix()).uri)
    //   case _ => super.source()
    // }
    PriceURI(feed,apiSuffix()).parse() match {
      case Some(uri) => source(uri)
      case None => super.source()
    }
  }

  override def source(feed:String) = {
    feed.split("://").toList match {
      case "http" :: _ | "https" :: _ => {
        // val reqs = tokensFilter.map( t => 
        //   HttpRequest(uri = feed.replaceFirst(TOKEN_SLOT,t)).withHeaders(Accept(MediaTypes.`application/json`))
        // )
        val reqs = Seq(
          HttpRequest(uri = feed.replaceFirst(TOKENS_SLOT,tokensFilter.mkString(","))).withHeaders(Accept(MediaTypes.`application/json`))
        )
        log.info(s"reqs=${reqs}")
        Flows.fromHttpList(reqs,par = 1, frameDelimiter = config.delimiter,frameSize = config.buffer, throttle = config.throttleSource)
      }
            
      case _ => super.source(feed)
    }
  }
}
