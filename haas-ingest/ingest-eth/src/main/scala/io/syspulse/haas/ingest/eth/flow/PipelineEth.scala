package io.syspulse.haas.ingest.eth.flow

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

import io.syspulse.haas.core.Tx
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthJson._

import io.syspulse.haas.ingest.eth.EthURI

abstract class PipelineEth[T <: skel.Ingestable](feed:String,output:String)(implicit config:Config,implicit val fmt:JsonFormat[T])
  extends Pipeline[T,T,T](feed,output,config.throttle,config.delimiter,config.buffer) {

  protected val log = com.typesafe.scalalogging.Logger(s"${this}")

  //import EthJson._

  def filter:Seq[String] = config.filter
  def apiSuffix():String

  override def source() = {
    feed.split("://").toList match {
      case "eth" :: _ => super.source(EthURI(feed,apiSuffix()).uri)
      case _ => super.source()
    }
  }

  override def processing:Flow[T,T,_] = Flow[T].map(v => v)

}
