package io.syspulse.haas.ingest.cg.store

import java.time.{Instant}
import java.nio.file.StandardOpenOption._

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.{Done, NotUsed}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

import akka.stream.ActorMaterializer
import akka.stream._
import akka.stream.scaladsl._

import akka.http.scaladsl._
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, MediaRanges,MediaTypes, HttpMethods }
import java.util.concurrent.TimeUnit

import akka.util.ByteString

import java.nio.file.{Path,Paths}

import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext.Implicits.global

import scala.jdk.CollectionConverters._

import io.syspulse.skel.util.Util

class CgStoreFile[T](logFile:String,toLog:(T => String)) extends CgStore[T] {

  override def getSink = if(logFile.trim.isEmpty) 
      Sink.ignore
    else
      Flow[T]
        .map(t=>s"${toLog(t)}\n")
        .map(ByteString(_))
        .to(FileIO.toPath(Paths.get(Util.toFileWithTime(logFile)),options =  Set(WRITE, CREATE)))

}