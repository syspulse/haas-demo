package io.syspulse.haas.ingest.icp

import io.syspulse.skel.Ingestable

case class Block(
  i:Long,       // block number
  hash:String,  // block hash 
  ts:Long       // timestamp

) extends Ingestable {
  override def getKey:Option[Any] = Some(i)
}
