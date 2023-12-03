package io.syspulse.haas.ingest.icp

import io.syspulse.skel.Ingestable


case class Operation(
  i:Long,           // operation index
  typ:String,       // type
  sts:String,       // status  
  addr:String,      // account addr
  v:String,         // value (String ??)
  curr:String,      // currency
  dec:Int,          // decimals
)

case class Transaction(  
  hash:String,      // transaction hash
  ops:Seq[Operation], // operations

  b:Long,           // block number
  ts:Long,          // timestamp
  
  i:Option[Long] = None,  // transaction index in Block

) extends Ingestable {
  override def getKey:Option[Any] = Some(hash)
}
