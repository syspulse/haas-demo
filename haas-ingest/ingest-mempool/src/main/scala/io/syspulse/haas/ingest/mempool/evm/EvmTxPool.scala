package io.syspulse.haas.ingest.mempool.evm

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.core.TokenBlockchain

case class EvmTx(
  ts:Long,
  pool:String, // queued, pending
  blockHash:Option[String],
  blockNumber:Option[Long],
  from: String,
  gas: Long,
  gasPrice: BigInt,
  maxFeePerGas: BigInt,
  maxPriorityFeePerGas: BigInt,
  hash: String,
  input: String,
  nonce: Long,
  to: String,
  transactionIndex: Option[Int],
  value: BigInt,
  `type`: Byte,
  accessList: List[String],
  chainId: Int,
  v: Byte,
  r: String,
  s: String
) extends Ingestable

case class EvmTxRaw(
  blockHash:Option[String],
  blockNumber:Option[Long],
  from: String, 
  gas: String,
  gasPrice: String,
  maxFeePerGas: String,
  maxPriorityFeePerGas: String,
  hash: String,
  input: String,
  nonce: String,
  to: String,
  transactionIndex: Option[Int],
  value: String,
  `type`: String,
  accessList: List[String],
  chainId: String,
  v: String,
  r: String,
  s: String

) {
  
  def unraw(ts:Long,pool:String):EvmTx = EvmTx(
      ts,
      pool,
      this.blockHash,
      this.blockNumber,
      this.from: String,
      java.lang.Long.parseLong(this.gas.drop(2),16),
      BigInt(Util.unhex(gasPrice)),
      BigInt(Util.unhex(maxFeePerGas)),
      BigInt(Util.unhex(maxPriorityFeePerGas)),
      this.hash,
      this.input,
      Integer.parseInt(nonce.drop(2),16),
      this.to,
      this.transactionIndex,
      BigInt(Util.unhex(value)),
      Integer.parseInt(`type`.drop(2),16).toByte,
      this.accessList,
      Integer.parseInt(chainId.drop(2),16),
      Integer.parseInt(v.drop(2),16).toByte,
      this.r,
      this.s
  )
}

case class EvmTxPoolResult(pending:Map[String,Map[String,EvmTxRaw]],queued:Map[String,Map[String,EvmTxRaw]])

case class EvmTxPool(
  jsonrpc:String,
  id:Long,
  result:EvmTxPoolResult
)
