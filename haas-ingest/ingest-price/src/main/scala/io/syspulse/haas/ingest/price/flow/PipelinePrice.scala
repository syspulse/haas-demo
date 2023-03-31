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
import io.syspulse.skel.serde.Parq._
import com.github.mjakubowski84.parquet4s.{ParquetRecordEncoder,ParquetSchemaResolver}

import java.util.concurrent.TimeUnit

import io.syspulse.haas.ingest.price.Config

import io.syspulse.haas.core.Price
import io.syspulse.haas.serde.PriceJson
import io.syspulse.haas.serde.PriceJson._

import io.syspulse.haas.ingest.price.PriceURI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

abstract class PipelinePrice[T](feed:String,output:String)(implicit config:Config,parqEncoders:ParquetRecordEncoder[T],parsResolver:ParquetSchemaResolver[T])
  extends Pipeline[T,T,Price](feed,output,config.throttle,config.delimiter,config.buffer,throttleSource = config.throttleSource) {

  protected val log = Logger(s"${this}")

  import PriceJson._

  val tokensFilter:Seq[String] = fromUri(config.tokens)

  def fromUri(uri:String):Seq[String] = {
    uri.split("://").toIndexedSeq.toList match {
      case "id" :: ids :: Nil => 
        ids.split(",").toIndexedSeq.filter(! _.trim.isEmpty)
      case "file" :: file :: Nil =>
        os.read(os.Path(file,os.pwd))
          .split("\n")
          .filter(!_.trim.isEmpty())
          .flatMap(_.split(",").toIndexedSeq.filter(!_.trim.isEmpty())).toSeq
      case ids  =>
        ids.mkString(",").split(",").toIndexedSeq.filter(! _.trim.isEmpty)      
    }
  }

  val TOKENS_SLOT = "_COINS_"

  def apiSuffix():String

  // URI encode
  def resolve(tokens:Seq[String]) = URLEncoder.encode(tokens.mkString(","), StandardCharsets.UTF_8.toString())

  def process:Flow[T,T,_] = Flow[T].map(v => v)

  override def source():Source[ByteString,_] = {
    PriceURI(feed,apiSuffix()).parse() match {
      case Some(uri) => source(uri)
      case None => super.source()
    }
  }
  
  override def source(feed:String) = {
    feed.split("://").toList match {
      case "http" :: _ | "https" :: _ => {
        // val reqs = tokensFilter.map( t => 
        //   HttpRequest(uri = feed.replaceFirst(TOKEN_SLOT,t)).withHeaders(Accept(MediaTypes.`application/json`))
        // )
        val reqs = tokensFilter.grouped(config.tokensLimit).map( tokenRange => {
          HttpRequest(uri = feed.replaceFirst(TOKENS_SLOT,resolve(tokenRange)))
              .withHeaders(Accept(MediaTypes.`application/json`))
          }).toSeq
        
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
