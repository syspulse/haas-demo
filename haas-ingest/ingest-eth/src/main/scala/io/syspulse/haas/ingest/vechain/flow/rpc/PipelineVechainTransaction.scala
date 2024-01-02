package io.syspulse.haas.ingest.vechain.flow.rpc

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

import io.syspulse.haas.ingest.vechain.Block
import io.syspulse.haas.ingest.vechain.Transaction
import io.syspulse.haas.ingest.vechain.VechainJson._

import io.syspulse.haas.ingest.vechain.flow.rpc._
import io.syspulse.haas.ingest.vechain.flow.rpc.VechainRpcJson._


abstract class PipelineVechainTransaction[E <: skel.Ingestable](config:Config)
                                                     (implicit val fmtE:JsonFormat[E],parqEncoders:ParquetRecordEncoder[E],parsResolver:ParquetSchemaResolver[E]) extends 
  PipelineVechain[RpcTx,RpcTx,E](config) {
    
  def apiSuffix():String = s"/transaction"

  def parse(data:String):Seq[RpcTx] = {
    val tx:Seq[RpcTx] = parseTransaction(data)    
   
    // ignore pending transactions
    if(tx.size!=0) {
      if(!tx.last.meta.isDefined) {
        log.warn(s"pending tx: ${tx.last.id}: ignore")
        Seq()
      } else {
        latestTs.set(tx.last.meta.get.blockTimestamp * 1000L)
        tx
      }      
    } else
      Seq()
  }

  def convert(tx:RpcTx):RpcTx = {
    tx
  }

  // def transform(tx: Transaction): Seq[Transaction] = {
  //   Seq(tx)
  // }
}

class PipelineTransaction(config:Config) extends PipelineVechainTransaction[Transaction](config) {    

  def transform(tx: RpcTx): Seq[Transaction] = {
    // ignore pending
    if(tx.meta.isDefined) {
      val txx = tx.clauses.map(clause => Transaction(
        ts = tx.meta.get.blockTimestamp,
        b = tx.meta.get.blockNumber,
        hash = tx.id,
        sz = tx.size,

        from = tx.origin,
        to = clause.to, 
        v = BigInt(clause.value),        // value
        nonce = tx.nonce,
        
        gas = tx.gas, 
        pric = tx.gasPriceCoef, 
        
        data = clause.data,

        blk = tx.blockRef,
        exp = tx.expiration,
        del = tx.delegator,
        dep = tx.dependsOn
      ))

      // commit cursor
      cursor.commit(tx.meta.get.blockNumber)
      txx
    } else 
      Seq()
  }
}
