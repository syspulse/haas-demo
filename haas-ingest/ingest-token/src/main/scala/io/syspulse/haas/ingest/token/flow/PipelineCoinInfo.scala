package io.syspulse.haas.ingest.token.flow

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


import io.syspulse.haas.ingest.coingecko.CoinGeckoTokenJson
import io.syspulse.haas.ingest.coingecko._
import io.syspulse.haas.ingest.coingecko.CoingeckoURI

import io.syspulse.haas.serde.TokenJson._

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.core.TokenBlockchain

import io.syspulse.haas.ingest.token.Config

class PipelineCoinInfo(feed:String,output:String)(implicit config:Config) extends PipelineToken[CoinInfo](feed,output) {

  import CoinGeckoTokenJson._

  val TOKEN_SLOT = "COIN"
  override def apiSuffix():String = s"/coins/${TOKEN_SLOT}?localization=false&tickers=false&market_data=false&community_data=false&developer_data=false&sparkline=false"
  
  override def fromUri(uri:String):Seq[String] = {
    uri.split("://").toList match {
      case "file" :: file :: Nil =>
        if(file.trim.toLowerCase().endsWith(".json")) {
          import CoinGeckoTokenJson._
          val json = os.read(os.Path(file,os.pwd))
          json.parseJson.convertTo[List[Coin]].map(_.id.trim).toSeq
        } else
          super.fromUri(uri)
      case _ =>
        super.fromUri(uri)
    }
  }

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
        val coin = data.split(",",-1).toList match {
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
    Seq(cg.toToken)
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
