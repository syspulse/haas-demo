package io.syspulse.haas.core

import io.syspulse.skel.Ingestable

case class Tx(
  ts:Long,
  i:Int,
  hash:String,
  blk:Long,
  from:String,
  to:Option[String],
  gas:Long,
  p:BigInt,
  inp:String,
  v:BigInt,

  non:Long,
  fee:Option[BigInt],
  tip:Option[BigInt], 
  typ:Int,      
  used2: Long,            // cumulative used
  used: Long,             // gas used
  cntr: Option[String],  // contract
  root: Option[String],   // receipt root
  sts: Int,               // status
  p2: BigInt              // price Effective

  //timestamp:Option[Long]
) extends Ingestable {
  override def getKey:Option[Any] = Some(hash)
}
