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

class PipelineEthTokenTransfer(feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String]) extends 
  PipelineEth[EthTokenTransfer,EthTokenTransfer](feed,output,throttle,delimiter,buffer,limit,size,filter) {
  
  override def apiSuffix():String = s"/"

  def transform(tx: EthTokenTransfer): Seq[EthTokenTransfer] = {
    Seq(tx)
  }

  override def parse(data:String):Seq[EthTokenTransfer] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val tt = data.parseJson.convertTo[EthTokenTransfer]
        
        val ts = tt.blockTimestamp
        latestTs.set(ts * 1000L)
        //Seq(tt.copy(blockTimestamp = ts))
        Seq(tt)

      } else {
        // ignore header
        if(data.stripLeading().startsWith("token_address")) {
          Seq.empty
        } else {
          val tt = data.split(",").toList match {
            case token_address :: from_address :: to_address :: 
                 value :: transaction_hash :: log_index :: 
                 block_number :: block_timestamp :: Nil =>
                
              if(filter.isEmpty || filter.contains(token_address)) {
                
                // ATTENTION: Stupid ethereum-etl insert '\r' !
                val ts = block_timestamp.trim.toLong
                latestTs.set(ts * 1000L)

                Seq(EthTokenTransfer(
                  token_address,
                  from_address,
                  to_address,
                  BigInt(value),                  
                  transaction_hash,
                  log_index.toInt,
                  block_number.toLong,
                  ts
                ))

              } else Seq.empty

            case _ => 
              log.error(s"failed to parse: '${data}'")
              Seq()
          }
          tt
        }
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }
}
