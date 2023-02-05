package io.syspulse.haas.ingest.price.flow

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

  def TOKENS_SLOT:String
  def apiSuffix():String

  def process:Flow[T,T,_] = Flow[T].map(v => v)

  override def source(feed:String) = {
    feed.split("://").toList match {
      case "http" :: _ | "https" :: _ => {
        // val reqs = tokensFilter.map( t => 
        //   HttpRequest(uri = feed.replaceFirst(TOKEN_SLOT,t)).withHeaders(Accept(MediaTypes.`application/json`))
        // )
        val reqs = Seq(
          HttpRequest(uri = feed.replaceFirst(TOKENS_SLOT,tokensFilter.mkString(",")))
            .withHeaders(Accept(MediaTypes.`application/json`))
        )
        log.info(s"reqs=${reqs}")

        val s = Source.tick(FiniteDuration(10,TimeUnit.MILLISECONDS), 
                            FiniteDuration(config.ingestCron.toLong,TimeUnit.SECONDS), 
                            s"ingest-${System.currentTimeMillis()}-${feed}")
        .map(h => {
          log.info(s"Cron --> ${h}")
          h
        })
        .via(
          Flows.fromHttpListAsFlow(reqs, par = 1, frameDelimiter = config.delimiter,frameSize = config.buffer, throttle = config.throttleSource)
        )
        s
      }
            
      case _ => super.source(feed)
    }
  }
}
