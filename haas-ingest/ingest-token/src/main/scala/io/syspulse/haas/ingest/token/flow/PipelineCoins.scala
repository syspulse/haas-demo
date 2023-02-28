package io.syspulse.haas.ingest.token.flow

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
import io.syspulse.haas.ingest.coingecko.CoinGeckoTokenJson
import io.syspulse.haas.ingest.coingecko._

import io.syspulse.haas.serde.TokenJson._

import io.syspulse.haas.ingest.token.Config

class PipelineCoins(feed:String,output:String)(implicit config:Config) extends PipelineToken[Coin](feed,output) {

  import CoinGeckoTokenJson._
  
  override def apiSuffix():String = s"/coins/list"

  def parse(data:String):Seq[Coin] = {
    if(data.isEmpty()) return Seq()
    
    try {
      val bulk = data.parseJson.convertTo[List[Coin]]
      log.debug(s"bulk=${bulk}")
      if(tokensFilter.size == 0)
        bulk.toSeq
      else
        bulk.filter( c => tokensFilter.contains(c.id) || tokensFilter.map(_.toLowerCase).contains(c.symbol.toLowerCase) ).toSeq
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def transform(cg: Coin): Seq[Token] = {
    val tt = Seq(Token(cg.id,cg.symbol,cg.name,None))
    log.debug(s"token=${tt}")
    tt
  }
}
