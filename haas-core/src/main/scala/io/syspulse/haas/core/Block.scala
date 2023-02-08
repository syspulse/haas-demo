package io.syspulse.haas.core

import io.syspulse.skel.Ingestable

case class Block(
  i:Long,       // block number
  hash:String,  
  phash:String, // parent hash
  non:String,   // noce
  uncl:String,  // uncles
  bloom:String, // bloom filter

  txrt:String, // tx root
  strt:String, // state root
  rert:String, // receipt root

  miner:String,
  dif:BigInt,
  dif0:BigInt, // total difficulty
  
  sz:Long,     // size
  data:String, // extra data
  used:Long,   // gas used
  limit:Long,  // gas limit

  ts:Long, 
  cnt:Long, // transaction count
  fee:Long, // base fee

) extends Ingestable {
  override def getKey:Option[Any] = Some(i)
}
