package io.syspulse.haas.core

import scala.jdk.CollectionConverters._
import io.syspulse.skel.Ingestable
import scala.collection.SortedSet


case class Holder(addr:String,bal:BigInt) extends Ordered[Holder] {
  // reverse ordering
  def compare(that: Holder):Int = {
    if (this.bal == that.bal) 0
    else if (this.bal > that.bal) -1
    else 1
  }
}

case class Holders(
  ts:Long,
  token:String,
  holders:Seq[Holder] = Seq(),
  total:Int = 0
    
  ) extends Ingestable with Ordered[Holders] {
  override def getKey:Option[Any] = Some(token)
  
  def compare(that: Holders):Int = {
    if (this.ts == that.ts)  0
    else if (this.ts > that.ts) 1
    else -1
  }
}

object Holders {
  type ID = String
}