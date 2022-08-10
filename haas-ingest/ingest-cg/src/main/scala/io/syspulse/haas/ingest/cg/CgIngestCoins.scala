package io.syspulse.haas.ingest.cg

import java.time.{Instant}

import akka.Done
import akka.actor._

import akka.http.scaladsl._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, MediaRanges,MediaTypes, HttpMethods }

import akka.stream._
import akka.stream.scaladsl.{ Sink, Source, Flow, FileIO, Tcp, RestartSource}
import akka.util.ByteString

import scala.concurrent.duration._
import java.nio.file.Paths

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

import scala.jdk.CollectionConverters._

import io.syspulse.skel.ingest.IngestClient
import io.syspulse.skel.util.Util._
import io.syspulse.skel.config.Configuration

import io.syspulse.haas.ingest.FeedIngest
import spray.json._

class CgIngestCoins(config:Config,c:Configuration) extends FeedIngest[CoingeckoCoin](config.cgUri,config.freq,config.limit,"Coingecko") {
  import CoingeckoJson._

  override def urls() = Seq(s"${host()}/coins")

  override def toData(json:String):Seq[CoingeckoCoin] = {
    log.debug(s"message: ${json}")
    val bulk = json.parseJson.convertTo[List[CoingeckoCoin]]
    if(config.tokens.isEmpty) 
      bulk.toSeq
    else
      bulk.filter( c => config.tokens.contains(c.id) || config.tokens.map(_.toLowerCase).contains(c.symbol.toLowerCase) ).toSeq
  }
}