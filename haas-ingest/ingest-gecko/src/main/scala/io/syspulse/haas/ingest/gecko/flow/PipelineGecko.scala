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

abstract class PipelineGecko[T](feed:String,output:String)(implicit config:Config)
  extends Pipeline[T,T,Token](feed,output,config.throttle,config.delimiter,config.buffer,throttleSource = config.throttleSource) {

  protected val log = com.typesafe.scalalogging.Logger(s"${this}")

  import CoingeckoJson._

  def tokensFilter:Seq[String] = config.tokens
  def apiSuffix():String

  override def source() = {
    feed.split("://").toList match {
      case "coingecko" :: _ => super.source(CoingeckoURI(feed,apiSuffix()).uri)
      case _ => super.source()
    }
  }

  override def processing:Flow[T,T,_] = Flow[T].map(v => v)

}
