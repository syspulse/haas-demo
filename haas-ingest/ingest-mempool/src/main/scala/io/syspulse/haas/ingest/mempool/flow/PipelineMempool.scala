package io.syspulse.haas.ingest.mempool

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

import akka.stream.scaladsl.Framing

import io.syspulse.haas.ingest.mempool.MempoolJson._

class PipelineMempool(feed:String,output:String)(implicit config:Config) extends Pipeline[MempoolTx,MempoolTx,MempoolTx](feed,output) {
  protected val log = Logger(s"${this}")  
  val sourceID = 0 // internal
  
  def parse(data:String):Seq[MempoolTx] = {
    //decoder.parse(data)
    log.warn(s"not implemented")
    Seq()
  }

  def process:Flow[MempoolTx,MempoolTx,_] = Flow[MempoolTx].map(v => v)
  def transform(p: MempoolTx): Seq[MempoolTx] = Seq(p)
}
