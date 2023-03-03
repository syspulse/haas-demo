package io.syspulse.haas.ingest.mempool

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util
import io.syspulse.haas.core.TokenBlockchain

case class MempoolTx(
  ts:Long,
  pool:Int, // pending - 0, queued - 1, 
  from: String,
  gas: Long,
  p: BigInt,
  fee: Option[BigInt], // old pre EIP-1155
  tip: Option[BigInt], // old transactions without tip
  hash: String,
  inp: String,
  non: Long,
  to: Option[String],
  v: BigInt,
  typ: Int,
  sig: String
) extends Ingestable
