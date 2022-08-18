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
import io.syspulse.skel.ingest.IngestFlow
import scala.concurrent.Awaitable

abstract class FeedFlow[T,D](uri:String,freq:Long,limit:Long,name:String = "",timeout:Long=5000L) extends IngestFlow[T,D] {
  private val log = com.typesafe.scalalogging.Logger(s"${this}")

  def host() = uri
  def urls() = Seq(s"${host()}/")
  
  override def source():Source[ByteString,_] = {
    val httpRequest = urls().map(url => 
      HttpRequest(uri = url)
        .withHeaders(Accept(MediaTypes.`application/json`))
    )

    val s1 = if(freq != 0) 
      Source.tick(0.seconds, FiniteDuration(freq,"seconds"), httpRequest)
    else 
      Source.single(httpRequest)
    
    val f2 = (
      if(limit == 0)
        s1
      else  
        s1.take(limit)
    )
    .mapConcat(identity)
    .mapAsync(par())(IngestFlow.fromHttp(_))
    .log(name)

    if(freq != 0L) 
      RestartSource.withBackoff(retrySettings) { () =>
        log.info(s"Connecting -> ${name}(${uri})...")  
        f2
      }
    else
      f2    
  }

  def par() = 1

  override def run() = {        
    val f = if(freq != 0L)
      super.run()
    else {      
      val f = super.run()
      log.info(s"${f}")
      f match {
        case a:Awaitable[_] => 
          Await.ready(a,FiniteDuration(timeout,TimeUnit.MILLISECONDS))
          // this is a very simple HACK to avoid Akka overengineering: java.lang.IllegalStateException: Pool shutdown unexpectedly
          Thread.sleep(500)
          system.terminate()
        case _ => 
      }      
    }
    f
  }
}