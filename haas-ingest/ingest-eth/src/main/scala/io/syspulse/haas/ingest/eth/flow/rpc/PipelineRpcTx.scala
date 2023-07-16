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

import io.syspulse.haas.core.Tx
import io.syspulse.haas.serde.TxJson
import io.syspulse.haas.serde.TxJson._
import io.syspulse.haas.ingest.eth.rpc._
import io.syspulse.haas.ingest.eth.rpc.EthRpcJson._
import io.syspulse.haas.ingest.eth.flow.PipelineEth
import io.syspulse.haas.ingest.eth.Config

import io.syspulse.haas.ingest.eth.flow.rpc.LastBlock

abstract class PipelineRpcTx[E <: skel.Ingestable](config:Config)
                                                  (implicit val fmtE:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E]) extends 
  PipelineRPC[RpcBlock,RpcBlock,E](config) {
  
  def apiSuffix():String = s"/tx"

  def parse(data:String):Seq[RpcBlock] = {
    val block = parseBlock(data)
    if(block.size!=0)
      latestTs.set(toLong(block.last.result.timestamp) * 1000L)
    
    block
  }

  def convert(block:RpcBlock):RpcBlock = {
    LastBlock.commit(toLong(block.result.number),block.result.hash)
    block    
  }
}

class PipelineTx(config:Config) 
  extends PipelineRpcTx[Tx](config) {

  def transform(block: RpcBlock): Seq[Tx] = {
    val ts = toLong(block.result.timestamp)
    val block_number = toLong(block.result.number)

    block.result.transactions.map{ tx => {
      val transaction_index = toLong(tx.transactionIndex).toInt
      Tx(
        ts * 1000L,
        transaction_index,
        tx.hash,
        block_number,

        tx.from,
        tx.to,
        
        toLong(tx.gas),
        toBigInt(tx.gasPrice),
        
        tx.input,
        toBigInt(tx.value),
        toLong(tx.nonce),
        
        None,//tx.max_fee_per_gas,
        None,//tx.max_priority_fee_per_gas, 

        toOptionLong(tx.`type`).map(_.toInt), 

        0L,//tx.receipt_cumulative_gas_used, 
        0L,//tx.receipt_gas_used, 
        None,//tx.receipt_contract_address, 
        None,//tx.receipt_root, 
        None,//tx.receipt_status, 
        None//tx.receipt_effective_gas_price
      )
    }}.toSeq
  }
}
