package io.syspulse.haas.intercept.flow.eth

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

import io.syspulse.haas.core.TokenTransfer
import io.syspulse.haas.ingest.eth.flow.PipelineEth

import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore

import io.syspulse.haas.intercept.Config
import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception
import io.syspulse.haas.intercept.InterceptionAlarm
import io.syspulse.haas.intercept.flow.eth.InterceptorTx
import io.syspulse.haas.intercept.store.ScriptStore
import io.syspulse.haas.intercept.store.InterceptionStore
import io.syspulse.haas.intercept.InterceptionJson._
import io.syspulse.haas.ingest.eth.flow.PipelineEthTokenTransfer

class PipelineEthInterceptTokenTransfer(feed:String,output:String,override val interceptor:Interceptor[TokenTransfer])(implicit config:Config) 
  //extends PipelineEthIntercept[TokenTransfer](feed,output,interceptor)(config) {
  extends PipelineEthTokenTransfer[InterceptionAlarm](feed,output,config.throttle,config.delimiter,config.buffer,config.limit,config.size,config.filter) 
  with PipelineEthIntercept[TokenTransfer] {

  //override def parse(data:String):Seq[TokenTransfer] = parseTokenTransfer(data)
}

