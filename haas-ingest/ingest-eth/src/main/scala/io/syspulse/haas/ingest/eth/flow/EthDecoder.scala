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

import io.syspulse.haas.serde._
import io.syspulse.haas.core.{ Block, Tx, TokenTransfer, Event }

import io.syspulse.haas.ingest.eth.EthURI

import io.syspulse.haas.ingest.eth.EthEtlJson
import io.syspulse.haas.ingest.eth.{EthBlock,EthTx,EthTokenTransfer,EthLog}

trait EthDecoder[T] {

  implicit val fmt:JsonFormat[T]

  protected val log = Logger(s"${this}")

  import EthEtlJson._
  import TxJson._
  import BlockJson._
  import TokenTransferJson._
  import EventJson._

  var latestTs:AtomicLong = new AtomicLong(0)

  def parseBlock(data:String):Seq[EthBlock] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val block = data.parseJson.convertTo[EthBlock]
        
        val ts = block.timestamp
        latestTs.set(ts * 1000L)

        // Seq(Block(block.number,block.hash,block.parent_hash,block.nonce,block.sha3_uncles,block.logs_bloom,block.transactions_root,block.state_root,
        //           block.receipts_root,block.miner,block.difficulty,block.total_difficulty,block.size,block.extra_data, 
        //           block.gas_limit, block.gas_used, block.timestamp, block.transaction_count,block.base_fee_per_gas))
        Seq(block)
      } else {
        // assume CSV
        // ignore header
        // 
        if(data.stripLeading().startsWith("number")) {
          Seq.empty
        } else {
          val block = data.split(",").toList match {
            case number :: hash :: parent_hash :: nonce :: sha3_uncles :: logs_bloom :: transactions_root :: 
                 state_root :: receipts_root :: miner :: difficulty :: total_difficulty :: size :: extra_data :: 
                 gas_limit :: gas_used :: timestamp :: transaction_count :: base_fee_per_gas :: Nil =>
                
                 val ts = timestamp.toLong
                 latestTs.set(ts * 1000L)

                 Seq(EthBlock(
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
          block
        }
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def parseTx(data:String):Seq[EthTx] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val tx = data.parseJson.convertTo[EthTx]
        
        val ts = tx.ts
        latestTs.set(ts * 1000L)

        // Seq(Tx(tx.ts,tx.txIndex,tx.hash,tx.blockNumber,tx.fromAddress,tx.toAddress,tx.gas,tx.gasPrice,tx.input,tx.value))
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

                 Seq(EthTx(
                    ts,
                    transaction_index.toInt,
                    hash,
                    block_number.toLong,
                    from_address.toLowerCase(),
                    Option(to_address.toLowerCase()),
                    gas.toLong,
                    BigInt(gas_price),
                    input,
                    BigInt(value)
                  ))
            // this is format of Tx. WHY !?
            case ts :: transaction_index :: hash :: block_number :: from_address :: to_address :: 
                 gas :: gas_price :: input :: value :: Nil =>
                
                 latestTs.set(ts.toLong)

                 Seq(
                  EthTx(
                    ts.toLong,
                    transaction_index.toInt,
                    hash,
                    block_number.toLong,
                    from_address.toLowerCase(),
                    Option(to_address.toLowerCase()),
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

  def parseTokenTransfer(data:String):Seq[EthTokenTransfer] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val tt = data.parseJson.convertTo[EthTokenTransfer]
        
        val ts = tt.blockTimestamp
        latestTs.set(ts * 1000L)

        // Seq(TokenTransfer(ts, tt.blockNumber, tt.tokenAddress.toLowerCase(), tt.from.toLowerCase(), tt.to.toLowerCase(), tt.value, tt.txHash))
        Seq(tt)

      } else {
        // assume CSV
        // ignore header
        // 
        if(data.stripLeading().startsWith("token_address")) {
          Seq.empty
        } else {
          val tt = data.split(",").toList match {
            case tokenAddress :: from_address :: to_address :: value :: 
              transaction_hash :: log_index :: block_number :: block_timestamp :: Nil =>
            
              // ATTENTION: Stupid ethereum-etl insert '\r' !
              val ts = block_timestamp.trim.toLong
              latestTs.set(ts * 1000L)

              Seq( EthTokenTransfer(
                tokenAddress,from_address,to_address,BigInt(value),
                transaction_hash,log_index.toInt,block_number.toLong,
                ts
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

  // def parseEvent(data:String):Seq[Event] = {
  //   if(data.isEmpty()) return Seq()

  //   try {
  //     // check it is JSON
  //     if(data.stripLeading().startsWith("{")) {
  //       val tt = data.parseJson.convertTo[EthLog]
        
  //       val ts = tt.block_timestamp
  //       latestTs.set(ts * 1000L)

  //       Seq(Event(ts, tt.block_number, tt.address, tt.data, tt.transaction_hash, tt.topics))
  //     } else {
  //       // assume CSV
  //       // ignore header
  //       // 
  //       if(data.stripLeading().startsWith("log_index")) {
  //         Seq.empty
  //       } else {
  //         val tt = data.split(",").toList match {
  //           case block_timestamp :: block_number :: address :: data :: 
  //             transaction_hash :: log_index :: topics :: Nil =>
            
  //             val ts = block_timestamp.toLong
  //             latestTs.set(ts * 1000L)

  //             Seq(Event(
  //               ts,
  //               block_number.toLong,
  //               address.toLowerCase(),
  //               data,
  //               transaction_hash,
  //               Util.csvToList(topics)
  //             ))      
  //           case _ => 
  //             log.error(s"failed to parse: '${data}'")
  //             Seq()
  //         }
  //         tt
  //       }
  //     }
  //   } catch {
  //     case e:Exception => 
  //       log.error(s"failed to parse: '${data}'",e)
  //       Seq()
  //   }
  // }

  def parseEventLog(data:String):Seq[EthLog] = {
    if(data.isEmpty()) return Seq()

    try {
      // check it is JSON
      if(data.stripLeading().startsWith("{")) {
        val tt = data.parseJson.convertTo[EthLog]
        
        val ts = tt.block_timestamp 
        latestTs.set(ts * 1000L )
        Seq(tt)

      } else {
        // ignore header
        if(data.stripLeading().startsWith("type")) {
          Seq.empty
        } else {
          val tt = data.split(",").toList match {
            case log_index :: transaction_hash :: transaction_index :: address :: 
                 data :: topics :: block_number :: block_timestamp :: block_hash :: 
                 item_id :: item_timestamp :: Nil =>
                
              // ATTENTION: Stupid ethereum-etl insert '\r' !
              val ts = block_timestamp.trim.toLong
              latestTs.set(ts * 1000L)

              Seq(EthLog(
                log_index.toInt,
                transaction_hash,
                transaction_index.toInt,
                address,
                data,
                List(topics),
                block_number.toLong,
                ts,
                block_hash
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

}
