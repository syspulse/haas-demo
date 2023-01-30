package io.syspulse.haas.core

import io.syspulse.skel.Ingestable

case class Event(
  ts:Long,
  block:Long,
  contract:String,
  data:String,
  hash:String,
  topics:List[String] = List()
  
) extends Ingestable {
  override def getKey:Option[Any] = Some(hash)
}
