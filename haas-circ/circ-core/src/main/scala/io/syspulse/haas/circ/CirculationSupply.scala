package io.syspulse.haas.circ

import scala.jdk.CollectionConverters._
import io.jvm.uuid._
import scala.math.BigInt

//import io.syspulse.skel.Ingestable
import io.syspulse.haas.core.Token
import scala.collection.SortedSet

// "buckets" is a labeled set: Investors, Founders, Hackers, DAO
case class SupplyBucket(
  label:String,  
  value:BigInt,  
  ratio:Double,
  change:Double = 0.0
)

case class SupplyCategory(
  label:String,  
  value:BigInt
)

// Circulations by Timestamp (daily snapshots)
case class Circulation (
  ts:Long,

  totalSupply:BigInt = 0,
  supply:BigInt = 0,
  inflation:Double = 0.0,
  price:Double = 0.0, // always USD precomputed
  marketCap:Double = 0.0, // always USD 

  buckets: List[SupplyBucket] = List(), // buckets of holders

  holders:Seq[SupplyHolder] = Seq.empty,
  holdersTotal:Long = 0L,
  holdersUp:Long = 0L,
  holdersDown:Long = 0L,

  category:List[SupplyCategory] = List(),

) extends Ordered[Circulation] {
  
  def compare (that: Circulation):Int = {
    if (this.ts == that.ts)
      0
    else if (this.ts > that.ts)
      1
    else
      -1
  }
}

// Circulation Supply 
case class CirculationSupply(
  id: CirculationSupply.ID,
  tokenId:Token.ID,  
  name:String = s"Supply",
  //history:SortedSet[Circulation] = SortedSet() // sorted list of Daily snapshots
  history:List[Circulation] = List() // sorted list of Daily snapshots

  ) {//extends Ingestable {
  
  def getKey:Option[Any] = Some(id)
}

object CirculationSupply {
  type ID = UUID

  def apply(id:String) = UUID(id)
}

