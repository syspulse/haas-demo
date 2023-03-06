package io.syspulse.haas.ingest.mempool

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import akka.actor.ActorSystem
import akka.util.ByteString

import akka.http.scaladsl.model.{HttpRequest,HttpMethods,HttpEntity,ContentTypes}
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

import io.syspulse.haas.core.DataSource

import akka.stream.scaladsl.Framing
import akka.http.javadsl.model.ContentType

import io.syspulse.haas.ingest.mempool.evm.{EvmTx,EvmTxPool}
import io.syspulse.haas.ingest.mempool.evm.EvmTxPoolJson._
import io.syspulse.haas.serde.MempoolJson._
import io.syspulse.skel.util.DiffSet
import io.syspulse.haas.ingest.mempool.evm.EvmTxRaw


class PipelineEvmTxPool(feed:String,output:String,delta:Boolean)(implicit config:Config) 
  extends Pipeline[EvmTx,EvmTx,EvmTx](feed:String,output:String,config.throttle,config.delimiter,config.buffer,chunk = 0){
  
  protected val log = Logger(s"${this}")

  val sourceID = DataSource.id("evm")
  
  var pendingDiff = new DiffSet[EvmTxRaw](Set())
  var queuedDiff = new DiffSet[EvmTxRaw](Set())

  def apiSuffix():String = ""

  override def source(feed:String) = {
    feed.split("://").toList match {
      case "http" :: _ | "https" :: _ => {
        val reqs = {
          val id = System.currentTimeMillis() / 1000L
          val json = s"""{
                "jsonrpc":"2.0","method":"txpool_content",
                "params":[],
                "id":${id}
              }""".trim.replaceAll("\\s+","")
          
          log.info(s"json='${json}'")
            
          HttpRequest( method = HttpMethods.POST, uri = feed,
              entity = HttpEntity(ContentTypes.`application/json`,json)
          ).withHeaders(Accept(MediaTypes.`application/json`))
        }
            
        log.info(s"reqs=${reqs}")

        val sourceHttp = Source.tick(FiniteDuration(10,TimeUnit.MILLISECONDS), 
                            FiniteDuration(config.ingestCron.toLong,TimeUnit.SECONDS), 
                            s"ingest-${System.currentTimeMillis()}-${feed}")
        .map(h => {
          log.info(s"Cron --> ${h}")
          h
        })
        .via(
          Flows.fromHttpListAsFlow(Seq(reqs), 
            par = 1, 
            frameDelimiter = config.delimiter,
            frameSize = config.buffer, 
            throttle = config.throttleSource)
        )
        sourceHttp
      }
            
      case _ => super.source(feed)
    }
  }

  def parse(data:String):Seq[EvmTx] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        try {
          val ts = System.currentTimeMillis
          val txpool = data.parseJson.convertTo[EvmTxPool]
          
          // val pending = txpool.result.pending.values.map(_.values).flatten.map(raw => raw.unraw(ts,"pending"))
          // val queued = txpool.result.queued.values.map(_.values).flatten.map(raw => raw.unraw(ts,"queued"))
          val pending = txpool.result.pending.values.map(_.values).flatten
          val queued = txpool.result.queued.values.map(_.values).flatten
          log.debug(s"pending=${pending}")
          log.debug(s"queued=${queued}")
          
          val (pendingNew,pendingOld,pendingOut,queuedNew,queuedOld,queuedOut) = 
          if(delta) {
            val pendingSet:Set[EvmTxRaw] = pending.toSet
            val queuedSet:Set[EvmTxRaw] = queued.toSet
            
            val (pendingNew,pendingOld,pendingOut) = pendingDiff.diff(pendingSet)
            val (queuedNew,queuedOld,queuedOut) = queuedDiff.diff(queuedSet)
            
            (pendingNew.toSeq,pendingOld.toSeq,pendingOut.toSeq,
             queuedNew.toSeq,queuedOld.toSeq,queuedOut.toSeq)
          } else
            (pending.toSeq,Seq(),Seq(),
            queued.toSeq,Seq(),Seq())

          log.info(s"pending=[${pendingNew.size},${pendingOld.size},${pendingOut.size}],queued=[${queuedNew.size},${queuedOld.size},${queuedOut.size}]")
          
          // convert EthTxRaw -> EthTx
          // ATTENTION: only New !
          pendingNew.map(raw => raw.unraw(ts,"queued")) ++ queuedNew.map(raw => raw.unraw(ts,"queued"))

        } catch {
          case e:Exception => 
            log.error(s"failed to parse: '${data}'",e)
            Seq()
        }
      } else {
        val m = data.split(",").toList match {
          case rpc :: id :: result :: Nil => 
            log.error(s"not implemented: '${data}'")
            None
          case _ => {
            log.error(s"failed to parse: '${data}'")
            None
          }
        }
        m.toSeq
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def process:Flow[EvmTx,EvmTx,_] = Flow[EvmTx].map(v => v)

  def transform(etx: EvmTx): Seq[EvmTx] = {        
    Seq(etx)
    // Seq(MempoolTx(
    //   ts = etx.ts,
    //   pool = (if(etx.pool == "pending") 0 else 1),
    //   from = etx.from,
    //   gas = etx.gas,
    //   p = etx.gasPrice,
    //   fee = etx.maxFeePerGas,
    //   tip = etx.maxPriorityFeePerGas,
    //   hash = etx.hash,
    //   inp = etx.input,
    //   non = etx.nonce,
    //   to = etx.to,
    //   v = etx.value,
    //   typ = etx.`type`,
    //   sig = s"${etx.r}:${etx.s}:${etx.v}"
    // ))
  }
}
