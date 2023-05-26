package io.syspulse.haas.ingest.eth.flow.etl

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
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._
import io.syspulse.haas.ingest.eth.flow.PipelineEth

abstract class PipelineETLTx[E <: skel.Ingestable](feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String])(implicit val fmtE:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E]) extends 
  PipelineEth[EthTx,Tx,E](feed,output,throttle,delimiter,buffer,limit,size,filter) with PipelineETL[E] {
  
  def apiSuffix():String = s"/tx"

  def parse(data:String):Seq[EthTx] = {
    val d = parseTx(data)
    if(d.size!=0)
      latestTs.set(d.last.block_timestamp * 1000L)
    d
  }

  def convert(tx:EthTx):Tx = Tx(
    tx.block_timestamp * 1000L,
    tx.transaction_index,
    tx.hash,
    tx.block_number,
    tx.from_address,
    tx.to_address,
    tx.gas,
    tx.gas_price,
    tx.input,
    tx.value,

    tx.nonce,
    tx.max_fee_per_gas,
    tx.max_priority_fee_per_gas, 
    tx.transaction_type, 
    tx.receipt_cumulative_gas_used, 
    tx.receipt_gas_used, 
    tx.receipt_contract_address, 
    tx.receipt_root, 
    tx.receipt_status, 
    tx.receipt_effective_gas_price
  )

}

class PipelineTx(feed:String,output:String,throttle:Long,delimiter:String,buffer:Int,limit:Long,size:Long,filter:Seq[String]) 
  extends PipelineETLTx[Tx](feed,output,throttle,delimiter,buffer,limit,size,filter) {

  def transform(tx: Tx): Seq[Tx] = Seq(tx)
}
