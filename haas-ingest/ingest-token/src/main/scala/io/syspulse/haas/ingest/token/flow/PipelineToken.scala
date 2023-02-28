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
import io.syspulse.haas.serde.TokenJson._

import io.syspulse.haas.ingest.token.Config
import io.syspulse.haas.ingest.token.TokenURI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

abstract class PipelineToken[T](feed:String,output:String)(implicit config:Config)
  extends Pipeline[T,T,Token](feed,output,config.throttle,config.delimiter,config.buffer,throttleSource = config.throttleSource) {

  protected val log = com.typesafe.scalalogging.Logger(s"${this}")

  def resolve(tokens:Seq[String]) = URLEncoder.encode(tokens.mkString(","), StandardCharsets.UTF_8.toString())
  
  // val tokensFilter:Seq[String] = config.tokens
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


  def apiSuffix():String = ???

  override def source():Source[ByteString,_] = {
    TokenURI(feed,apiSuffix()).parse() match {
      case Some(uri) => source(uri)
      case None => super.source()
    }
  }
  
  // override def source() = {
  //   feed.split("://").toList match {
  //     case "coingecko" :: _ => super.source(CoingeckoURI(feed,apiSuffix()).uri)
  //     case _ => super.source()
  //   }
  // }

  def process:Flow[T,T,_] = Flow[T].map(v => v)

}
