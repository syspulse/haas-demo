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
import io.syspulse.haas.ingest.eth.intercept.InterceptorTx
import java.util.concurrent.atomic.AtomicLong
import io.syspulse.haas.core.Block

class PipelineEthLog(feed:String,output:String)(implicit config:Config) extends PipelineEth[EthLog,EthLog](feed,output) {
  
  override def apiSuffix():String = s"/"

  def transform(tx: EthLog): Seq[EthLog] = {
    Seq(tx)
  }

  override def parse(data:String):Seq[EthLog] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val tt = data.parseJson.convertTo[EthLog]
        
        val ts = tt.block_timestamp * 1000L
        latestTs.set(ts)
        Seq(tt.copy(block_timestamp = ts))

      } else {
        // ignore header
        if(data.stripLeading().startsWith("type")) {
          Seq.empty
        } else {
          val tt = data.split(",").toList match {
            case log_index :: transaction_hash :: transaction_index :: address :: data :: topics :: block_number :: block_timestamp :: block_hash :: 
                 item_id :: item_timestamp :: Nil =>
                
              if(filter.isEmpty || filter.contains(address)) {
                
                // ATTENTION: Stupid ethereum-etl insert '\r' !
                val ts = block_timestamp.trim.toLong * 1000L
                latestTs.set(ts)

                Seq(EthLog(
                  log_index.toInt,
                  transaction_hash,
                  transaction_index.toInt,
                  address,
                  data,
                  List(topics),
                  block_number.toLong,
                  ts,
                  block_hash
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
