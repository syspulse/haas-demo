package io.syspulse.haas.core

import io.syspulse.skel.Ingestable

case class TokenTransfer(
  ts:Long,
  block:Long,
  contract:String,
  from:String,
  to:String,
  value:BigInt,
  hash:String,
  
) extends Ingestable {
  override def getKey:Option[Any] = Some(hash)
}
