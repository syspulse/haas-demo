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

import io.syspulse.haas.core.Tx
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._
import java.util.concurrent.atomic.AtomicLong
import io.syspulse.haas.core.Block

import io.syspulse.haas.core.Event
import io.syspulse.haas.serde.EventJson
import io.syspulse.haas.serde.EventJson._
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._


abstract class PipelineEthLog[E <: skel.Ingestable](feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String])(implicit val fmtE:JsonFormat[E]) extends 
  PipelineEth[EthLog,Event,E](feed,output,throttle,delimiter,buffer,limit,size,filter) {
  
  def apiSuffix():String = s"/event"

  // override def parse(data:String):Seq[EthLog] = {
  //   if(data.isEmpty()) return Seq()

  //   try {
  //     // check it is JSON
  //     if(data.stripLeading().startsWith("{")) {
  //       val tt = data.parseJson.convertTo[EthLog]
        
  //       val ts = tt.block_timestamp 
  //       latestTs.set(ts * 1000L )
  //       Seq(tt)

  //     } else {
  //       // ignore header
  //       if(data.stripLeading().startsWith("type")) {
  //         Seq.empty
  //       } else {
  //         val tt = data.split(",").toList match {
  //           case log_index :: transaction_hash :: transaction_index :: address :: data :: topics :: block_number :: block_timestamp :: block_hash :: 
  //                item_id :: item_timestamp :: Nil =>
                
  //             if(filter.isEmpty || filter.contains(address)) {
                
  //               // ATTENTION: Stupid ethereum-etl insert '\r' !
  //               val ts = block_timestamp.trim.toLong
  //               latestTs.set(ts * 1000L)

  //               Seq(EthLog(
  //                 log_index.toInt,
  //                 transaction_hash,
  //                 transaction_index.toInt,
  //                 address,
  //                 data,
  //                 List(topics),
  //                 block_number.toLong,
  //                 ts,
  //                 block_hash
  //               ))

  //             } else Seq.empty

  //           case _ => 
  //             log.error(s"failed to parse: '${data}'")
  //             Seq()
  //         }
  //         tt
  //       }
  //     }
  //   } catch {
  //     case e:Exception => 
  //       log.error(s"failed to parse: '${data}'",e)
  //       Seq()
  //   }
  // }

  override def parse(data:String):Seq[EthLog] = parseEventLog(data)

  def convert(e:EthLog):Event = Event(e.block_timestamp * 1000L, e.block_number, e.address, e.data,e.transaction_hash,e.topics )

  // def transform(e: Event): Seq[Event] = {
  //   Seq(e)
  // }
}

class PipelineLog(feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String]) 
  extends PipelineEthLog[Event](feed,output,throttle,delimiter,buffer,limit,size,filter) {

  def transform(e: Event): Seq[Event] = Seq(e)
}

