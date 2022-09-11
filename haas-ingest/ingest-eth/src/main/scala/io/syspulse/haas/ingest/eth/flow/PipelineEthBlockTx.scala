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
import java.util.concurrent.atomic.AtomicLong
import io.syspulse.haas.core.Block

class PipelineEthBlockTx(feed:String,output:String)(implicit config:Config) extends PipelineEth[Tx,Tx](feed,output) {
  import EthJson._
  override def apiSuffix():String = s"/"

  var latestTs:AtomicLong = new AtomicLong(0)

  def parse(data:String):Seq[Tx] = {
    if(data.isEmpty()) return Seq()

    try {
      // detect if it is block
      if(data.contains(""""type": "block"""")) {
        val block = data.parseJson.convertTo[Block]
        latestTs.set(block.timestamp)
        // skip it
        Seq.empty
      } else {
        val tx = data.parseJson.convertTo[Tx]        
        Seq(tx.copy(timestamp = Some(latestTs.get)))
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq.empty
    }
  }

  def transform(tx: Tx): Seq[Tx] = {
    Seq(tx)
  }
}
