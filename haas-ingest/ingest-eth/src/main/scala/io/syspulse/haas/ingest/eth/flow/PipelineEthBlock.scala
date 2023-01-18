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

import io.syspulse.haas.core.Block
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._


class PipelineEthBlock(feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String]) extends 
  PipelineEth[Block,Block](feed,output,throttle,delimiter,buffer,limit,size,filter) {

  import EthEtlJson._
  
  override def apiSuffix():String = s"/"

  def parse(data:String):Seq[Block] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val block = data.parseJson.convertTo[Block]
        
        val ts = block.timestamp
        latestTs.set(ts * 1000L)

        Seq(block)
      } else {
        // assume CSV
        // ignore header
        // 
        if(data.stripLeading().startsWith("number")) {
          Seq.empty
        } else {
          val tx = data.split(",").toList match {
            case number :: hash :: parent_hash :: nonce :: sha3_uncles :: logs_bloom :: transactions_root :: 
                 state_root :: receipts_root :: miner :: difficulty :: total_difficulty :: size :: extra_data :: 
                 gas_limit :: gas_used :: timestamp :: transaction_count :: base_fee_per_gas :: Nil =>
                
                 val ts = timestamp.toLong
                 latestTs.set(ts * 1000L)

                 Seq(Block(
                    number.toLong,
                    hash,parent_hash, nonce, 
                    sha3_uncles,logs_bloom,
                    transactions_root,state_root, receipts_root,
                    miner, BigInt(difficulty), BigInt(total_difficulty),
                    size.toLong,
                    extra_data,
                    gas_limit.toLong,gas_used.toLong,

                    ts,
                    transaction_count.toLong,
                    base_fee_per_gas.toLong                    
                  ))            
                  
            case _ => 
              log.error(s"failed to parse: '${data}'")
              Seq()
          }
          tx
        }
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def transform(block: Block): Seq[Block] = {
    Seq(block)
  }
}
