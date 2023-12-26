package io.syspulse.haas.ingest.icp.flow.rosetta

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import akka.util.ByteString

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

import io.syspulse.haas.ingest.Config

import io.syspulse.haas.ingest.icp.Block
import io.syspulse.haas.ingest.icp.Transaction
import io.syspulse.haas.ingest.icp.Operation
import io.syspulse.haas.ingest.icp.IcpJson._

import io.syspulse.haas.ingest.icp.flow.rosetta._
import io.syspulse.haas.ingest.icp.flow.rosetta.IcpRpcJson._


abstract class PipelineIcpTransaction[E <: skel.Ingestable](config:Config)
                                                     (implicit val fmtE:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E]) extends 
  PipelineIcp[IcpRpcTransaction,IcpRpcTransaction,E](config) {
    
  def apiSuffix():String = ""

  def parse(data:String):Seq[IcpRpcTransaction] = {
    val bb = parseBlock(data)    
   
    if(bb.size!=0) {
      latestTs.set(bb.last.block.timestamp * 1000L)
      
      bb.flatMap(b => {
        b.block.transactions
      })

    } else
      Seq()
  }

  def convert(tx:IcpRpcTransaction):IcpRpcTransaction = {
    tx
  }

  // def transform(block: Block): Seq[Block] = {
  //   Seq(block)
  // }
}

class PipelineTansaction(config:Config) extends PipelineIcpTransaction[Transaction](config) {    

  def transform(tx: IcpRpcTransaction): Seq[Transaction] = {
    val t = Transaction(            
      tx.transaction_identifier.hash,
      ops = tx.operations.map(o => Operation(
        o.operation_identifier.index,
        o.`type`,
        o.status,
        o.account.address,
        o.amount.value,
        o.amount.currency.symbol,
        o.amount.currency.decimals,
      )),
      
      tx.metadata.block_height,
      tx.metadata.timestamp / 1000000L,      
    )

    // commit cursor
    cursor.commit(t.b)

    Seq(t)
  }    
}
