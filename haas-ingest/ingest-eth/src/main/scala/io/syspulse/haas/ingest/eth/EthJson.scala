package io.syspulse.haas.ingest.eth

import com.typesafe.scalalogging.Logger

// ATTENTION
import io.syspulse.skel.service.JsonCommon
import spray.json._
import spray.json.{DefaultJsonProtocol,NullOptions}

import io.syspulse.haas.core.Tx
import io.syspulse.haas.core.Block

object EthJson extends JsonCommon with NullOptions {
  import DefaultJsonProtocol._
  implicit val jf_tx = jsonFormat(Tx,"block_timestamp","transaction_index","hash","block_number","from_address","to_address","gas","gas_price","input","value","timestamp")
  implicit val jf_bl = jsonFormat(Block,
  "number","hash","parent_hash","nonce",
  "sha3_uncles","logs_bloom","transactions_root", "state_root", "receipts_root", "miner", "difficulty", "total_difficulty", "size", "extra_data", "gas_limit", "gas_used", "timestamp",
  "transaction_count","base_fee_per_gas")
}
