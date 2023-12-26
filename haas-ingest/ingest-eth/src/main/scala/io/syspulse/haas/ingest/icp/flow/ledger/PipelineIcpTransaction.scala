package io.syspulse.haas.ingest.icp.flow.ledger

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

import io.syspulse.haas.ingest.icp.flow.ledger._
import io.syspulse.haas.ingest.icp.flow.ledger.IcpRpcJson._

// The concept of Blocks in Ledger API is somewhat ambiguous
// IcpRcpBlock has Transaction info inside
abstract class PipelineIcpTransaction[E <: skel.Ingestable](config:Config)
                                                     (implicit val fmtE:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E]) extends 
  PipelineIcp[IcpRpcBlock,IcpRpcBlock,E](config) {
    
  def apiSuffix():String = ""

  def parse(data:String):Seq[IcpRpcBlock] = {
    val bb = parseBlock(data)    
    if(bb.size!=0) {
      val b = bb.last
      latestTs.set(b.created_at * 1000L)      
    }
    bb
  }

  def convert(block:IcpRpcBlock):IcpRpcBlock = {
    block
  }

  // def transform(block: Block): Seq[Block] = {
  //   Seq(block)
  // }
}

class PipelineTansaction(config:Config) extends PipelineIcpTransaction[Transaction](config) {    

  def transform(b: IcpRpcBlock): Seq[Transaction] = {
    val tx = Transaction(            
      b.transaction_hash,
      ops = Seq(),
      
      b.block_height.toLong,
      b.created_at * 1000L,      
    )

    // commit cursor
    cursor.commit(tx.b)
    Seq(tx)
  }    
}
