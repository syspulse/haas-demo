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

import io.syspulse.haas.ingest.price._
import io.syspulse.haas.ingest.price.CoinGeckoPriceJson

import io.syspulse.haas.serde.PriceJson._
import io.syspulse.haas.ingest.price.PriceURI
import akka.stream.scaladsl.Framing
import io.syspulse.haas.serde.PriceDecoder
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import io.syspulse.haas.core.resolver.TokenResolverMem

class PipelineCoinGecko(feed:String,output:String)(implicit config:Config) extends PipelinePrice[CoinGeckoPrice](feed:String,output:String){
  
  import CoinGeckoPriceJson._

  val sourceID = DataSource.id("coingecko")
  val TOKENS_SLOT = "COINS"

  def resolve(tokens:Seq[String]) = tokens.mkString(",")

  def apiSuffix():String = s"?ids=${resolve(config.tokens)}&vs_currencies=${config.tokensPair.mkString(",")}"
    //s"?fsyms=${TOKENS_SLOT}&tsyms=${config.tokensPair.mkString(",")}"

  override def source():Source[ByteString,_] = {
    PriceURI(feed,apiSuffix()).parse() match {
      case Some(uri) => source(uri)
      case None => super.source()
    }
  }

  def parse(data:String):Seq[CoinGeckoPrice] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        try {
          val obj = data.parseJson.convertTo[Map[String,Map[String,Double]]]          
          //log.info(s"price=${price}")
          Seq(CoinGeckoPrice(pairs = obj,ts = Some(System.currentTimeMillis)))

        } catch {
          case e:Exception => 
            log.error(s"failed to parse: '${data}'",e)
            Seq()
        }
      } else {
        val price = data.split(",").toList match {
          case id :: ts :: v :: Nil => 
            Some(CoinGeckoPrice(pairs = Map(id -> Map("usd" -> v.toDouble))))
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

   def transform(cct: CoinGeckoPrice): Seq[Price] = {
    cct.pairs.map{ case(token,pair) =>
      pair.map{ case(p,price) => 
        config.priceFormat match {
          case "price" => Price(token, cct.ts.getOrElse(0L), price, pair = Some(p), src = sourceID)
          case "telemetry" => Price(s"${token}-${p}", cct.ts.getOrElse(0L), price, None, src = sourceID)
        }
        
      }      
    }.flatten.toSeq
  }
}
