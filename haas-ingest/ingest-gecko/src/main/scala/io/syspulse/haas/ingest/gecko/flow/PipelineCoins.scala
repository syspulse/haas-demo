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

import io.syspulse.haas.ingest.TokenJson._

class PipelineCoins(feed:String,output:String)(implicit config:Config)
  extends Pipeline[CoingeckoCoin,CoingeckoCoin,Token](feed,output,config.throttle,config.delimiter,config.buffer) {

  private val log = com.typesafe.scalalogging.Logger(s"${this}")

  import CoingeckoJson._

  def tokensFilter:Seq[String] = config.tokens

  def parse(data:String):Seq[CoingeckoCoin] = {
    try {
      val bulk = data.parseJson.convertTo[List[CoingeckoCoin]]
      if(tokensFilter.size == 0)
        bulk.toSeq
      else
        bulk.filter( c => tokensFilter.contains(c.id) || tokensFilter.map(_.toLowerCase).contains(c.symbol.toLowerCase) ).toSeq
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'")
        Seq()
    }
  }
  
  override def processing:Flow[CoingeckoCoin,CoingeckoCoin,_] = Flow[CoingeckoCoin].map(v => v)

  def transform(cg: CoingeckoCoin): Seq[Token] = {
    Seq(Token(cg.id,cg.symbol,cg.name,None))
  }
}
