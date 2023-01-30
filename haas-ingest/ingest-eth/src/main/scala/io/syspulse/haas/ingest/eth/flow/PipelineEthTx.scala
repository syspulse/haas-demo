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
import io.syspulse.haas.serde.TxJson
import io.syspulse.haas.serde.TxJson._
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._

class PipelineEthTx(feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String]) extends 
  PipelineEth[EthTx,Tx](feed,output,throttle,delimiter,buffer,limit,size,filter) {
  
  override def apiSuffix():String = s"/"

  override def parse(data:String):Seq[EthTx] = parseTx(data)

  def transform(tx: EthTx): Seq[Tx] = {
    // Seq(tx)
    Seq(Tx(tx.ts,tx.txIndex,tx.hash,tx.blockNumber,tx.fromAddress,tx.toAddress,tx.gas,tx.gasPrice,tx.input,tx.value))
  }
}
