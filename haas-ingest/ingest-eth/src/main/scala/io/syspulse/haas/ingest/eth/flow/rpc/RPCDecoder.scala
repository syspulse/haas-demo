package io.syspulse.haas.ingest.eth.flow.rpc

import java.util.concurrent.atomic.AtomicLong
import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import com.github.tototoshi.csv._

import io.syspulse.skel
import io.syspulse.skel.config._
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.skel.ingest._

import spray.json._
import DefaultJsonProtocol._
import java.util.concurrent.TimeUnit

import io.syspulse.haas.serde._
import io.syspulse.haas.core.{ Block, Tx, TokenTransfer, Event }

import io.syspulse.haas.ingest.eth.EthURI

import io.syspulse.haas.ingest.eth.rpc._
import io.syspulse.haas.ingest.eth.rpc.EthRpcJson

import io.syspulse.haas.ingest.eth.flow.EthDecoder

trait RPCDecoder[T] extends EthDecoder[T,RpcBlock,RpcTx,RpcTokenTransfer,RpcLog] {

  protected val log = Logger(s"${this}")

  import EthRpcJson._
  import TxJson._
  import BlockJson._
  import TokenTransferJson._
  import EventJson._

  def toLong(data:String) = java.lang.Long.parseLong(data.stripPrefix("0x"),16)
  def toBigInt(data:String) = BigInt(Util.unhex(data))
  def toOption(data:String) = if(data.isEmpty() || data=="0x") None else Some(data)
  def toOptionLong(data:String) = if(data.isEmpty() || data=="0x") None else Some(toLong(data))

  def parseBlock(data:String):Seq[RpcBlock] = {
    if(data.isEmpty()) return Seq()

    try {
      // only JSON is supported
      if(data.stripLeading().startsWith("{")) {
        val block = data.parseJson.convertTo[RpcBlock]
                
        Seq(block)
      } else {
        log.error(s"failed to parse: '${data}'")
        throw new RetryException(s"failed to parse: '${data}'")        
        Seq()
      }        
    } catch {
      case e:Exception => 
        //log.error(s"failed to parse: '${data}'",e)
        log.error(s"failed to parse: '${data}'")
        throw new RetryException(s"failed to parse: '${data}'")
        Seq()
    }
  }

  def parseTx(data:String):Seq[RpcTx] = {
    if(data.isEmpty()) return Seq()

    try {
      // Only Json from Block is supported
      if(data.stripLeading().startsWith("{")) {
        val block = data.parseJson.convertTo[RpcBlock]
                
        block.result.transactions
      } else {
        log.error(s"failed to parse: '${data}'")
        Seq()          
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }
  
  def parseTokenTransfer(data:String):Seq[RpcTokenTransfer] = {
    Seq()
  }

  
  def parseEventLog(data:String):Seq[RpcLog] = {
    Seq()
  }

}
