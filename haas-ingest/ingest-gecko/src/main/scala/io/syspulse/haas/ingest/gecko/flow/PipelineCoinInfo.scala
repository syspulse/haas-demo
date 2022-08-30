package io.syspulse.haas.ingest.gecko.flow

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

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

import spray.json._
import DefaultJsonProtocol._
import java.util.concurrent.TimeUnit

import io.syspulse.haas.core.Token
import io.syspulse.haas.ingest.gecko.CoingeckoJson
import io.syspulse.haas.ingest.gecko._

import io.syspulse.haas.token.TokenJson._
import io.syspulse.haas.ingest.gecko.CoingeckoURI

class PipelineCoinInfo(feed:String,output:String)(implicit config:Config) extends PipelineGecko[CoinInfo](feed,output) {

  import CoingeckoJson._

  override def apiSuffix():String = s"/coins/${config.tokens.mkString(",")}"
  override def processing:Flow[CoinInfo,CoinInfo,_] = Flow[CoinInfo].map(v => v)

  def parse(data:String):Seq[CoinInfo] = {
    try {
      val coin = data.parseJson.convertTo[CoinInfo]
      log.info(s"coin=${coin}")
      Seq(coin)
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def transform(cg: CoinInfo): Seq[Token] = {    
    Seq(Token(cg.id,cg.symbol,cg.name,cg.contract_address))
  }
}
