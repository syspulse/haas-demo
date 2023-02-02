package io.syspulse.haas.ingest.price.flow

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

import io.syspulse.haas.ingest.price.Config

import io.syspulse.haas.core.Price
import io.syspulse.haas.serde.PriceJson
import io.syspulse.haas.serde.PriceJson._

import io.syspulse.haas.ingest.price.PriceURI

abstract class PipelinePrice[T](feed:String,output:String)(implicit config:Config)
  extends Pipeline[T,T,Price](feed,output,config.throttle,config.delimiter,config.buffer,throttleSource = config.throttleSource) {

  protected val log = Logger(s"${this}")

  import PriceJson._

  val tokensFilter:Seq[String] = config.tokens

  def apiSuffix():String

  def process:Flow[T,T,_] = Flow[T].map(v => v)

}
