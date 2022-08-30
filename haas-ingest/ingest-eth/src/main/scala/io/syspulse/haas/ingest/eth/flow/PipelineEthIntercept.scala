package io.syspulse.haas.ingest.eth.flow

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

import io.syspulse.haas.core.Tx
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthJson._
import io.syspulse.haas.ingest.eth.intercept.InterceptorTx

import io.syspulse.haas.ingest.eth.intercept.Interception
import io.syspulse.haas.ingest.eth.intercept.InterceptionJson

import InterceptionJson._

class PipelineEthIntercept(feed:String,output:String)(implicit config:Config) extends PipelineEth[Tx,Interception](feed,output) with EthTx {

  val interceptor = new InterceptorTx(config)
  
  override def apiSuffix():String = s"/"

  def transform(tx: Tx): Seq[Interception] = {
    interceptor.scan(tx)
  }
}
