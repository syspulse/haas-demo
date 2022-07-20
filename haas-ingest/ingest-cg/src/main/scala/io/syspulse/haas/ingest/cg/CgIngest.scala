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

class CgIngest[T](config:Config,c:Configuration) extends IngestClient {
    
  def url(host:String = "http://localhost:8100", tokens:Seq[String] = Seq("UNI")) = s"${host}/"
  def getRequest(req: HttpRequest) = httpFlow(req)

  def toData(json:String):Seq[T] = {
    log.info(s"message: ${json}")
    Seq()
  }

  def createSource() = {        
    val freq = FiniteDuration(config.freq,"seconds")
    
    val httpRequest = HttpRequest(uri = url(config.cgUri)).withHeaders(Accept(MediaTypes.`application/json`))
    val source = Source.tick(0.seconds, freq, httpRequest)
    
    if(config.limit == 0)
      source
    else  
      source.take(config.limit)
  }

  def run(sink1:Sink[T,Future[Done]],sink2:Sink[T,Future[Done]] = Sink.ignore) = {
    val source = createSource()
    
    val restartableSource = RestartSource.withBackoff(retrySettings) { () =>
      log.info(s"Connecting -> CG(${config.cgUri})...")
      source
        .mapAsync(1)(getRequest(_))
        .map(countFlow)
        .log("CG")
        .map(toJson(_))
        .mapConcat(toData(_))
    }

    val flow = Flow[T].map(e => {println(s"${e}"); e;})
    val stream = restartableSource
      .alsoTo(sink1)
      .via(flow)
      .runWith(sink2)
    stream
  }
}