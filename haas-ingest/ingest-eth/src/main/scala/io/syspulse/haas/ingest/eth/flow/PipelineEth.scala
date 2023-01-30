package io.syspulse.haas.ingest.eth.flow

import java.util.concurrent.atomic.AtomicLong
import io.syspulse.skel.ingest.flow.Flows

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

import io.syspulse.haas.core.{ Block, Tx, TokenTransfer, Event }
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._

import io.syspulse.haas.ingest.eth.EthURI

abstract class PipelineEth[T,O <: skel.Ingestable](feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String],reportFreq:Long = 100000)(implicit val fmt:JsonFormat[O])
  extends Pipeline[T,T,O](feed,output,throttle,delimiter,buffer) with EthDecoder[O] {

  import EthEtlJson._
  
  override def getRotator():Flows.Rotator = 
    new Flows.RotatorTimestamp(() => {
      latestTs.get()
    })

  override def getFileLimit():Long = limit
  override def getFileSize():Long = size

  def filter():Seq[String] = filter
  def apiSuffix():String

  override def source() = {
    feed.split("://").toList match {
      case "eth" :: _ => super.source(EthURI(feed,apiSuffix()).uri)
      case _ => super.source()
    }
  }

  override def processing:Flow[T,T,_] = Flow[T].map(v => {
    if(countObj % reportFreq == 0)
      log.info(s"processed: ${countInput},${countObj}")
    v
  })

}
