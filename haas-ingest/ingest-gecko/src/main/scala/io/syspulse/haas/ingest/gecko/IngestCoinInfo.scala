package io.syspulse.haas.ingest.gecko

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
import spray.json._

import io.syspulse.skel.ingest.IngestClient
import io.syspulse.skel.util.Util._
import io.syspulse.skel.config.Configuration

import io.syspulse.haas.ingest.FeedIngest
import io.syspulse.haas.ingest.FeedFlow
import io.syspulse.haas.ingest.gecko._
import io.syspulse.haas.core.Token

class IngestCoinInfo(config:Config,c:Configuration) 
  extends CoingeckoFlow[CoingeckoCoinInfo](config.feed,config.output,config.freq,config.limit,"Coingecko") 
  with FlowCoinInfo {
  
  override val log = com.typesafe.scalalogging.Logger(s"${this}")

  override def urls() = config.tokens.map( c => (s"${host()}/coins/${c}"))

  override def flow = Flow[CoingeckoCoinInfo].throttle(1,1.second)

}