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
import io.syspulse.skel.serde.Parq._
import com.github.mjakubowski84.parquet4s.{ParquetRecordEncoder,ParquetSchemaResolver}

import java.util.concurrent.TimeUnit

import io.syspulse.haas.core.Block
import io.syspulse.haas.serde.BlockJson
import io.syspulse.haas.serde.BlockJson._
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._

import io.syspulse.haas.ingest.eth.flow.PipelineEth

abstract class PipelineEthBlock[E <: skel.Ingestable](feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String])(implicit val fmtE:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E]) extends 
  PipelineEth[EthBlock,Block,E](feed,output,throttle,delimiter,buffer,limit,size,filter) {
  
  def apiSuffix():String = s"/block"

  def parse(data:String):Seq[EthBlock] = parseBlock(data)

  def convert(block:EthBlock):Block = 
      Block(block.number,block.hash,block.parent_hash,block.nonce,block.sha3_uncles,block.logs_bloom,block.transactions_root,block.state_root,
            block.receipts_root,block.miner,block.difficulty,block.total_difficulty,block.size,block.extra_data, 
            block.gas_limit, block.gas_used, 
            block.timestamp * 1000L, 
            block.transaction_count,block.base_fee_per_gas)

  // def transform(block: Block): Seq[Block] = {
  //   Seq(block)
  // }
}

class PipelineBlock(feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String]) 
  extends PipelineEthBlock[Block](feed,output,throttle,delimiter,buffer,limit,size,filter) {

  def transform(block: Block): Seq[Block] = {
    Seq(block)
  }    
}
