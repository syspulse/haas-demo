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
import io.syspulse.skel.elastic.ElasticFlow
import io.syspulse.skel.Ingestable
import akka.stream.scaladsl.RestartSink

abstract class Feeder
case class HttpFeeder(http:HttpRequest) extends Feeder
case class FileFeeder(file:String) extends Feeder

abstract class FeedFlow[T,D <: Ingestable](feed:String,output:String,freq:Long,limit:Long,name:String = "",timeout:Long=5000L) extends IngestFlow[T,D] {
  private val log = com.typesafe.scalalogging.Logger(s"${this}")

  def host() = feed
  def urls() = Seq(s"${host()}/")
  

  override def source() = {
    log.info(s"feed=${feed}")

    val s0 = feed.split("://").toList match {
      case "http" :: _ => IngestFlow.fromHttp(HttpRequest(uri = feed).withHeaders(Accept(MediaTypes.`application/json`)),frameDelimiter = "\r",frameSize = 1024 * 1024 * 1024)
      case "https" :: _ => IngestFlow.fromHttp(HttpRequest(uri = urls().head).withHeaders(Accept(MediaTypes.`application/json`)),frameDelimiter = "\r",frameSize = 1024 * 1024 * 1024)
      case "file" :: fileName :: Nil => IngestFlow.fromFile(fileName,1024)
      case "stdin" :: _ => IngestFlow.fromStdin()
      case _ => IngestFlow.fromFile(feed)
    }

    // val s1 = if(freq != 0) 
    //   Source.tick(0.seconds, FiniteDuration(freq,"seconds"), ())
    // else 
    //   Source.single(s0)
    
    // val s2 = (
    //   if(limit == 0)
    //     s1
    //   else  
    //     s1.take(limit)
    // )

    // s2.mapConcat(s0)
    
    RestartSource.onFailuresWithBackoff(RestartSettings(3.seconds,10.seconds,0.2)) { () =>
      log.info(s"Connecting -> ${name}(${urls()})...")  
      s0
    }
  }

  override def sink() = {
    val sink = output.split("://").toList match {
      //case "elastic" :: _ => 
      case "file" :: fileName :: Nil => IngestFlow.toFile(fileName)
      case "hive" :: fileName :: Nil => IngestFlow.toHiveFile(fileName)
      case "stdout" :: _ => IngestFlow.toStdout()
      case Nil => IngestFlow.toStdout()
      case _ => IngestFlow.toFile(output)
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

  // override def source():Source[ByteString,_] = {

  //   val s0 = 
  //     if(feed.trim.toLowerCase().startsWith("http://") || feed.trim.toLowerCase().startsWith("https://")) {
  //         urls().map(url => 
  //           HttpFeeder(
  //             HttpRequest(uri = url).withHeaders(Accept(MediaTypes.`application/json`))
  //           )
  //         )
  //       } else {
  //         urls().map( url => FileFeeder(url))
  //       }
      

  //   val s1 = if(freq != 0) 
  //     Source.tick(0.seconds, FiniteDuration(freq,"seconds"), s0)
  //   else 
  //     Source.single(s0)
    
  //   val s2 = (
  //     if(limit == 0)
  //       s1
  //     else  
  //       s1.take(limit)
  //   )

  //   val s3 = s2
  //   .mapConcat(identity)
  //   .mapAsync(par())( f => f match {
  //     case HttpFeeder(http) => IngestFlow.fromHttpFuture(http)
  //     case FileFeeder(file) => IngestFlow.fromFile(file)
  //   })

  //   if(freq != 0L) 
  //     RestartSource.withBackoff(retrySettings) { () =>
  //       log.info(s"Connecting -> ${name}(${feed})...")  
  //       s3
  //     }
  //   else
  //     s3  
  // }

  // def par() = 1

  // override def run() = {        
  //   val f = if(freq != 0L)
  //     super.run()
  //   else {      
  //     val f = super.run()
  //     log.info(s"${f}")
  //     f match {
  //       case a:Awaitable[_] => 
  //         Await.ready(a,FiniteDuration(timeout,TimeUnit.MILLISECONDS))
  //         // this is a very simple HACK to avoid Akka overengineering: java.lang.IllegalStateException: Pool shutdown unexpectedly
  //         Thread.sleep(500)
  //         system.terminate()
  //       case _ => 
  //     }      
  //   }
  //   f
  // }
}