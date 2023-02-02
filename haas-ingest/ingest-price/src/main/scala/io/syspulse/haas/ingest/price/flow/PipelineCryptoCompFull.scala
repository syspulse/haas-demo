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

class PipelineCryptoCompFull(feed:String,output:String)(implicit config:Config) 
  extends PipelineCryptoComp[CryptoCompFull](feed,output) {

  import CryptoCompJson._

  override def apiSuffix():String = "pricemultifull" + super.apiSuffix()

  def parseCryptoCompFull(data:String):Seq[CryptoCompFull] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        try {
          val price = data.parseJson.convertTo[CryptoCompFull]
          //log.info(s"price=${price}")
          Seq(price)
        } catch {
          case e:Exception => 
            log.error(s"failed to parse: '${data}'")
            Seq()
        }
      } else {
        // assume csv
        val price = data.split(",").toList match {
          case id :: ts :: v :: Nil => 
            Some(CryptoCompFull(`RAW` = Map(id -> Map("USD" -> CryptoCompData(id,v.toDouble,ts.toLong)))))
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

  def parse(data:String):Seq[CryptoCompFull] = {
    log.debug(s"data=${data}")
    if(data.isEmpty()) return Seq()

    parseCryptoCompFull(data)
  }

  def transform(p: CryptoCompFull): Seq[Price] = {
    p.`RAW`.map{ case(token,pair) =>
      pair.map{ case(p,info) => 
        config.priceFormat match {
          case "price" => Price(token, info.`LASTUPDATE` * 1000L, info.`PRICE`,pair = Some(p), src = sourceID)
          case "telemetry" => Price(s"${token}-${p}", info.`LASTUPDATE` * 1000L, info.`PRICE`,None, src = sourceID)
        }
        
      }      
    }.flatten.toSeq
  }

}
