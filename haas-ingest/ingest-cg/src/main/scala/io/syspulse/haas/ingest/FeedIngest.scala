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

abstract class FeedIngest[T](uri:String,freq:Long,limit:Long,name:String = "") extends IngestClient {
    
  def host() = uri
  def urls() = Seq(s"${host()}/")
  def getRequest(req: HttpRequest) = httpFlow(req)

  def toData(json:String):Seq[T] = {
    log.info(s"message: ${json}")
    Seq()
  }

  def createSource() = {            
    val httpRequest = urls().map(url => 
      HttpRequest(uri = url)
        .withHeaders(Accept(MediaTypes.`application/json`))
    )

    val source = Source.tick(0.seconds, FiniteDuration(freq,"seconds"), httpRequest)
    
    if(limit == 0)
      source
    else  
      source.take(limit)
  }

  def flow() = Flow[T].map(m => m)

  def par() = 1

  def run(sink1:Sink[T,Future[Done]],sink2:Sink[T,Future[Done]] = Sink.ignore) = {
    val source = createSource()
    
    val restartableSource = RestartSource.withBackoff(retrySettings) { () =>
      log.info(s"Connecting -> ${name}(${uri})...")
      source
        .mapConcat(identity)
        .mapAsync(par())(getRequest(_))
        .map(countFlow)
        .log(name)
        .map(toJson(_))
        .mapConcat(toData(_))
    }
    
    val stream = restartableSource
      .alsoTo(sink1)
      .via(flow())
      .runWith(sink2)
    stream
  }
}