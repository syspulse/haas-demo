package io.syspulse.haas.ingest.eth.flow.rpc

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
import io.syspulse.skel.serde.Parq._
import com.github.mjakubowski84.parquet4s.{ParquetRecordEncoder,ParquetSchemaResolver}

import java.util.concurrent.TimeUnit

import io.syspulse.haas.core.Block
import io.syspulse.haas.serde.BlockJson
import io.syspulse.haas.serde.BlockJson._
import io.syspulse.haas.ingest.eth.rpc._
import io.syspulse.haas.ingest.eth.rpc.EthRpcJson._
import io.syspulse.haas.ingest.eth.flow.PipelineEth


abstract class PipelineRpcBlock[E <: skel.Ingestable](feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String])(implicit val fmtE:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E]) extends 
  PipelineEth[RpcBlock,Block,E](feed,output,throttle,delimiter,buffer,limit,size,filter) with PipelineRPC[E]{
  
  def apiSuffix():String = s"/block"

  def parse(data:String):Seq[RpcBlock] = {
    val d = parseBlock(data)
    if(d.size!=0)
      latestTs.set(toLong(d.last.result.timestamp) * 1000L)
    d
  }

  def convert(block:RpcBlock):Block = 
      Block(
        toLong(block.result.number),
        block.result.hash,
        block.result.parentHash,
        block.result.nonce,
        block.result.sha3Uncles,        
        block.result.logsBloom,
        block.result.transactionsRoot,
        block.result.stateRoot,        
        block.result.receiptsRoot,
        block.result.miner,
        
        toBigInt(block.result.difficulty),
        toBigInt(block.result.totalDifficulty),
        toLong(block.result.size),

        block.result.extraData, 
            
        toLong(block.result.gasLimit), 
        toLong(block.result.gasUsed), 
        toLong(block.result.timestamp) * 1000L, 
        block.result.transactions.size,
        block.result.baseFeePerGas.map(d => toLong(d))
      )

  // def transform(block: Block): Seq[Block] = {
  //   Seq(block)
  // }
}

class PipelineBlock(feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String]) 
  extends PipelineRpcBlock[Block](feed,output,throttle,delimiter,buffer,limit,size,filter) {

  def transform(block: Block): Seq[Block] = {
    Seq(block)
  }    
}
