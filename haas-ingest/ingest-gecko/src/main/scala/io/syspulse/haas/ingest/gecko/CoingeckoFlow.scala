package io.syspulse.haas.ingest.gecko

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
import io.syspulse.haas.core.Token

import io.syspulse.haas.ingest.FeedFlow

abstract class CoingeckoFlow[D](uri:String,output:String,freq:Long,limit:Long,name:String = "",timeout:Long=5000L) 
  extends FeedFlow[D,Token](uri,output,freq,limit,name) {
  
  
}