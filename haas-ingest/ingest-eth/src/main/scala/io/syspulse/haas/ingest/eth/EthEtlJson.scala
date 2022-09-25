package io.syspulse.haas.ingest.eth

import com.typesafe.scalalogging.Logger

// ATTENTION
import io.syspulse.skel.service.JsonCommon
import io.syspulse.skel.Ingestable

import spray.json._
import spray.json.{DefaultJsonProtocol,NullOptions}

import io.syspulse.haas.core.Tx
import io.syspulse.haas.core.Block

case class TokenTransfer(tokenAddress:String,from:String,to:String,value:BigInt,txHash:String,logIndex:Int,blockNumber:Long,blockTimestamp:Long) extends Ingestable

case class EthLog(log_index:Int,transaction_hash:String,transaction_index:Int,address:String,data:String,topics: List[String], block_number: Long, block_timestamp:Long, block_hash:String) extends Ingestable

object EthEtlJson extends JsonCommon with NullOptions {
  import DefaultJsonProtocol._
  implicit val jf_tx = jsonFormat(Tx,"block_timestamp","transaction_index","hash","block_number","from_address","to_address","gas","gas_price","input","value")
  implicit val jf_bl = jsonFormat(Block,
  "number","hash","parent_hash","nonce",
  "sha3_uncles","logs_bloom","transactions_root", "state_root", "receipts_root", "miner", "difficulty", "total_difficulty", "size", "extra_data", "gas_limit", "gas_used", "timestamp",
  "transaction_count","base_fee_per_gas")
  implicit val jf_tt = jsonFormat(TokenTransfer,"token_address","from_address","to_address","value","transaction_hash","log_index","block_number","block_timestamp")
  implicit val jf_lo = jsonFormat(EthLog,"log_index","transaction_hash","transaction_index","address","data","topics","block_number","block_timestamp","block_hash")
}
