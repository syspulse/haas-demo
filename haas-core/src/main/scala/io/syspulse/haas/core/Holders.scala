package io.syspulse.haas.core

import scala.jdk.CollectionConverters._
import io.syspulse.skel.Ingestable

case class Holders(
  ts:Long,
  token:String,
  addrs:Map[String,BigInt] = Map()
    
  ) extends Ingestable with Ordered[Holders] {
  override def getKey:Option[Any] = Some(token)
  
  def compare (that: Holders):Int = {
    if (this.ts == that.ts)  0
    else if (this.ts > that.ts) 1
    else -1
  }
}

object Holders {
  type ID = String
}