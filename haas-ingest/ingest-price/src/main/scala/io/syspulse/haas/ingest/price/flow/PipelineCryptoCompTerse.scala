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
import java.util.concurrent.TimeUnit

import io.syspulse.haas.core.Price
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.ingest.price.feed.CryptoCompPriceJson
import io.syspulse.haas.ingest.price._

import io.syspulse.haas.serde.PriceJson._
import io.syspulse.haas.ingest.price.PriceURI
import io.syspulse.haas.serde.PriceDecoder

import io.syspulse.haas.ingest.price.feed.CryptoCompPriceTerse
class PipelineCryptoCompTerse(feed:String,output:String)(implicit config:Config) 
  extends PipelineCryptoComp[CryptoCompPriceTerse](feed,output) {

  import CryptoCompPriceJson._

  override def apiSuffix():String = "pricemulti" + super.apiSuffix()

  def parseCryptoComp(data:String):Seq[CryptoCompPriceTerse] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        try {
          val obj = data.parseJson.convertTo[Map[String,Map[String,Double]]]
          
          //log.info(s"price=${price}")
          Seq(CryptoCompPriceTerse(pairs = obj,ts = Some(System.currentTimeMillis)))
        }
        catch {
          case e:Exception => 
            log.error(s"failed to parse: '${data}'")
            Seq()
        }
      } else {
        // assume csv
        val price = data.split(",",-1).toList match {
          case id :: ts :: v :: Nil => 
            //Some(CryptoCompTerse(pairs = Map(id -> Map("USD" -> CryptoCompData(id,v.toDouble,ts.toLong)))))
            None
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

  def parse(data:String):Seq[CryptoCompPriceTerse] = {
    log.debug(s"data=${data}")
    if(data.isEmpty()) return Seq()

    parseCryptoComp(data)
  }

  def transform(cct: CryptoCompPriceTerse): Seq[Price] = {
    cct.pairs.map{ case(t,pair) =>
      pair.map{ case(p,price) =>        
        val token = idResolver.resolve(t).getOrElse(t)
        config.priceFormat match {
          case "price" => Price(token, cct.ts.getOrElse(0L), price, pair = Some(p), src = sourceID)
          case "telemetry" => Price(s"${token}-${p}", cct.ts.getOrElse(0L), price, None, src = sourceID)
        }
        
      }      
    }.flatten.toSeq
  }

}
