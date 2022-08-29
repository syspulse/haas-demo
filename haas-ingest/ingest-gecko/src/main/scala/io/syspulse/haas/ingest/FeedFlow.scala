package io.syspulse.haas.ingest

import java.time.{Instant}

import akka.Done
import akka.actor._

import akka.http.scaladsl._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, MediaRanges,MediaTypes, HttpMethods }

import akka.stream._
import akka.stream.scaladsl.{ Sink, Source, Flow, FileIO, Tcp, RestartSource}
import akka.stream.scaladsl.RestartSink
import akka.util.ByteString
import scala.concurrent.Awaitable

import scala.concurrent.duration._
import java.nio.file.Paths

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

import scala.jdk.CollectionConverters._

import io.syspulse.skel.util.Util._
import io.syspulse.skel.config.Configuration

import io.syspulse.skel.ingest.IngestClient
import io.syspulse.skel.ingest.IngestFlow
import io.syspulse.skel.elastic.ElasticFlow
import io.syspulse.skel.Ingestable
import io.syspulse.skel.ingest.flow.Flows

abstract class Feeder
case class HttpFeeder(http:HttpRequest) extends Feeder
case class FileFeeder(file:String) extends Feeder

abstract class FeedFlow[T,D <: Ingestable](feed:String,output:String,freq:Long,limit:Long,name:String = "",timeout:Long=5000L) extends IngestFlow[T,T,D] {
  private val log = com.typesafe.scalalogging.Logger(s"${this}")

  def host() = feed
  def urls() = Seq(s"${host()}/")
  

  override def source() = {
    log.info(s"feed=${feed}")

    val s0 = feed.split("://").toList match {
      case "http" :: _ => Flows.fromHttp(HttpRequest(uri = feed).withHeaders(Accept(MediaTypes.`application/json`)),frameDelimiter = "\r",frameSize = 1024 * 1024 * 1024)
      case "https" :: _ => Flows.fromHttp(HttpRequest(uri = urls().head).withHeaders(Accept(MediaTypes.`application/json`)),frameDelimiter = "\r",frameSize = 1024 * 1024 * 1024)
      case "file" :: fileName :: Nil => Flows.fromFile(fileName,1024)
      case "stdin" :: _ => Flows.fromStdin()
      case _ => Flows.fromFile(feed)
    }

    
    RestartSource.onFailuresWithBackoff(RestartSettings(3.seconds,10.seconds,0.2)) { () =>
      log.info(s"Connecting -> ${name}(${urls()})...")  
      s0
    }
  }

  override def sink() = {
    val sink = output.split("://").toList match {
      //case "elastic" :: _ => 
      case "file" :: fileName :: Nil => Flows.toFile(fileName)
      case "hive" :: fileName :: Nil => Flows.toHiveFile(fileName)
      case "stdout" :: _ => Flows.toStdout()
      case Nil => Flows.toStdout()
      case _ => Flows.toFile(output)
    }
    
    RestartSink.withBackoff(RestartSettings(3.seconds,10.seconds,0.2)) { () =>
      log.info(s"Sinking -> ${output}")  
      sink
    }
  }

  override def run() = {
    val r = super.run()

    if(freq != 0L)
      r
    else {
      r match {
        case a: Awaitable[_] => 
          Await.ready(a,FiniteDuration(timeout,TimeUnit.MILLISECONDS))      
          system.terminate()
      }      
    }
  }

  
}