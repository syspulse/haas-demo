package io.syspulse.haas.ingest.starknet

import io.syspulse.skel.Ingestable

case class Transaction(  
  hash:String,      // transaction hash
  nonce:Long,
  from:String,
  fee:BigInt,
  typ:String,
  ver:Int,
  sig:String,
  
  data:Seq[String], // calldata

  b:Long,           // block number
  ts:Long,          // timestamp
  
  i:Option[Long] = None,  // transaction index in Block

) extends Ingestable {
  override def getKey:Option[Any] = Some(hash)
}
