package io.syspulse.haas.ingest.eth.flow

import java.util.concurrent.atomic.AtomicLong
import io.syspulse.skel.ingest.flow.Flows

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

import io.syspulse.haas.core.{ Tx, TokenTransfer }
import io.syspulse.haas.ingest.eth._
import io.syspulse.haas.ingest.eth.EthEtlJson._

import io.syspulse.haas.ingest.eth.EthURI

abstract class PipelineEth[T,O <: skel.Ingestable](feed:String,output:String,reportFreq:Long = 100000)(implicit config:Config,implicit val fmt:JsonFormat[O])
  extends Pipeline[T,T,O](feed,output,config.throttle,config.delimiter,config.buffer) {

  protected val log = Logger(s"${this}")

  import EthEtlJson._

  var latestTs:AtomicLong = new AtomicLong(0)

  override def getRotator():Flows.Rotator = 
    new Flows.RotatorTimestamp(() => {
      latestTs.get()
    })

  override def getFileLimit():Long = config.limit
  override def getFileSize():Long = config.size

  def parseTx(data:String):Seq[Tx] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val tx = data.parseJson.convertTo[Tx]
        
        val ts = tx.ts
        latestTs.set(ts * 1000L)

        Seq(tx)
      } else {
        // assume CSV
        // ignore header
        // hash,nonce,block_hash,block_number,transaction_index,from_address,to_address,value,gas,gas_price,input,block_timestamp,max_fee_per_gas,max_priority_fee_per_gas,transaction_type
        if(data.stripLeading().startsWith("hash")) {
          Seq.empty
        } else {
          val tx = data.split(",").toList match {
            case hash :: nonce :: block_hash :: block_number :: transaction_index :: from_address :: to_address :: 
                 value :: gas :: gas_price :: input :: block_timestamp :: max_fee_per_gas :: max_priority_fee_per_gas :: 
                 transaction_type :: Nil =>
                
                 val ts = block_timestamp.toLong
                 latestTs.set(ts * 1000L)

                 Seq(Tx(
                    ts,
                    transaction_index.toInt,
                    hash,
                    block_number.toLong,
                    from_address,
                    Option(to_address),
                    gas.toLong,
                    BigInt(gas_price),
                    input,
                    BigInt(value)
                  ))
            // this is format of Tx. WHY !?
            case ts :: transaction_index :: hash :: block_number :: from_address :: to_address :: 
                 gas :: gas_price :: input :: value :: Nil =>
                
                 latestTs.set(ts.toLong)

                 Seq(Tx(
                    ts.toLong,
                    transaction_index.toInt,
                    hash,
                    block_number.toLong,
                    from_address,
                    Option(to_address),
                    gas.toLong,
                    BigInt(gas_price),
                    input,
                    BigInt(value)
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

  def parseTokenTransfer(data:String):Seq[TokenTransfer] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val tt = data.parseJson.convertTo[EthTokenTransfer]
        
        val ts = tt.blockTimestamp
        latestTs.set(ts * 1000L)

        Seq(TokenTransfer(ts, tt.blockNumber, tt.tokenAddress, tt.from, tt.to, tt.value, tt.txHash))
      } else {
        // assume CSV
        // ignore header
        // 
        if(data.stripLeading().startsWith("contract_address")) {
          Seq.empty
        } else {
          val tt = data.split(",").toList match {
            case contract_address :: from_address :: to_address :: value :: 
                 transaction_hash :: log_index :: block_number :: block_timestamp :: Nil =>
                
                 val ts = block_timestamp.toLong
                 latestTs.set(ts * 1000L)

                 Seq(TokenTransfer(
                    ts,
                    block_number.toLong,
                    contract_address,
                    from_address,
                    to_address,
                    BigInt(value),
                    transaction_hash
                  ))      
            case _ => 
              log.error(s"failed to parse: '${data}'")
              Seq()
          }
          tt
        }
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def filter:Seq[String] = config.filter
  def apiSuffix():String

  override def source() = {
    feed.split("://").toList match {
      case "eth" :: _ => super.source(EthURI(feed,apiSuffix()).uri)
      case _ => super.source()
    }
  }

  override def processing:Flow[T,T,_] = Flow[T].map(v => {
    if(countObj % reportFreq == 0)
      log.info(s"processed: ${countInput},${countObj}")
    v
  })

}
