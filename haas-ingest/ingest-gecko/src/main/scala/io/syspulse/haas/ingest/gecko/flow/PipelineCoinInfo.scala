package io.syspulse.haas.ingest.gecko.flow

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

import io.syspulse.haas.core.Token
import io.syspulse.haas.ingest.gecko.CoingeckoJson
import io.syspulse.haas.ingest.gecko._

import io.syspulse.haas.serde.TokenJson._
import io.syspulse.haas.ingest.gecko.CoingeckoURI
import akka.stream.scaladsl.Framing
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.core.TokenBlockchain

class PipelineCoinInfo(feed:String,output:String)(implicit config:Config) extends PipelineGecko[CoinInfo](feed,output) {

  import CoingeckoJson._

  val TOKEN_SLOT = "COIN"
  override def apiSuffix():String = s"/coins/${TOKEN_SLOT}?localization=false&tickers=false&market_data=false&community_data=false&developer_data=false&sparkline=false"
  
  def parse(data:String):Seq[CoinInfo] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        val coin = data.parseJson.convertTo[CoinInfo]
        log.info(s"coin=${coin}")
        Seq(coin)
      } else {
        // assume csv
        // FIX ME !
        val coin = data.split(",").toList match {
          case id :: symbol :: name :: contract_address :: category :: icon :: Nil => 
            Some(CoinInfo(id,symbol,name,Some(contract_address),categories = Util.csvToList(category),image = Image("","",icon)))
          case id :: symbol :: name :: contract_address :: Nil => Some(CoinInfo(id,symbol,name,Some(contract_address)))
          case id :: symbol :: name :: Nil => Some(CoinInfo(id,symbol,name,None))
          case _ => {
            log.error(s"failed to parse: '${data}'")
            None
          }
        }
        coin.toSeq
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def transform(cg: CoinInfo): Seq[Token] = {    
    // Seq(Token(
    //   cg.id,cg.symbol,cg.name,
    //   cg.contract_address,
    //   cg.categories,
    //   icon = Some(cg.image.large),
    //   src = Some(DataSource.id("coingecko")),
    //   dcml = cg.detail_platforms.get(cg.asset_platform_id.getOrElse("ethereum")).map(_.decimal_place),
    //   chain = cg.detail_platforms.map{ case(nid,dp) => TokenBlockchain(nid,dp.contract_address)}.toSeq
    // ))
    Seq(cg.toToken)
  }

  override def source() = {
    feed.split("://").toList match {
      case "coingecko" :: _ => source(CoingeckoURI(feed,apiSuffix()).uri) 
      case "http" :: _ | "https" :: _ => source(feed + apiSuffix())
      case _ => super.source()
    }
  }

  override def source(feed:String) = {
    feed.split("://").toList match {
      case "http" :: _ | "https" :: _ => {
        val reqs = tokensFilter.map( t => 
          HttpRequest(uri = feed.replaceFirst(TOKEN_SLOT,t)).withHeaders(Accept(MediaTypes.`application/json`))
        )
        log.info(s"reqs=${reqs}")
        Flows.fromHttpList(reqs,par = 1, frameDelimiter = config.delimiter,frameSize = config.buffer, throttle = config.throttleSource)
      }
            
      case _ => super.source(feed)
    }
  }
}
