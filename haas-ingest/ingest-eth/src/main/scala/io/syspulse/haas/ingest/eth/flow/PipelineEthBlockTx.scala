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
import io.syspulse.skel.ingest.flow.Flows

import spray.json._
import DefaultJsonProtocol._
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._

import io.syspulse.haas.core.Block

import io.syspulse.haas.core.Tx
import io.syspulse.haas.serde.TxJson
import io.syspulse.haas.serde.TxJson._
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._

import io.syspulse.haas.core.Block
import io.syspulse.haas.serde.BlockJson
import io.syspulse.haas.serde.BlockJson._

// class PipelineEthBlockTx(feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String]) extends 
//   PipelineEth[EthTx,Tx](feed,output,throttle,delimiter,buffer,limit,size,filter) {
  
//   override def apiSuffix():String = s"/block-tx"

//   def parse(data:String):Seq[EthTx] = {
//     if(data.isEmpty()) return Seq()

//     try {
//       // detect if it is block
//       if(data.contains(""""type": "block"""")) {
//         val block = data.parseJson.convertTo[EthBlock]
//         latestTs.set(block.timestamp)
//         // skip it
//         Seq.empty
//       } else {
//         val tx = data.parseJson.convertTo[EthTx]        
//         Seq(tx.copy(ts = latestTs.get))
//       }
//     } catch {
//       case e:Exception => 
//         log.error(s"failed to parse: '${data}'",e)
//         Seq.empty
//     }
//   }
  
  
//   def convert(tx: EthTx) = 
//     Tx(tx.ts,tx.txIndex,tx.hash,tx.blockNumber,tx.fromAddress,tx.toAddress,tx.gas,tx.gasPrice,tx.input,tx.value)


//   // def transform(tx: Tx): Seq[Tx] = {
//   //   Seq(tx)
//   // }
// }
