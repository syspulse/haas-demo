package io.syspulse.haas.supply

import scala.jdk.CollectionConverters._
import io.jvm.uuid._
import scala.math.BigInt

//import io.syspulse.skel.Ingestable
import io.syspulse.haas.core.Token
import scala.collection.SortedSet

case class TokenAddr(b:String,a:String)

case class BlockchainValues(b:String,v:BigInt)

case class TotalSupply(t:BigInt,bv:Seq[BlockchainValues])
case class CircSupply(t:BigInt,bv:Seq[BlockchainValues])

case class Category(cat:String,t:BigInt,bb:Seq[BlockchainValues])

case class LockInfo(a:String,tag:String,v:BigInt)
case class Lock(b:String,locks:Seq[LockInfo])

case class HolderBlockchainValues(b:String,v:Long)
case class Holders(t:Long,hh:Seq[HolderBlockchainValues])

case class TV(t:BigInt,bb:Seq[BlockchainValues])
case class TopHolders(addr:String,r:Double,tv:TV)

// Supplys by Timestamp (daily snapshots)
case class SupplyData (
  ts:Long,

  totalSupply:TotalSupply,
  circSupply:CircSupply,

  inflation:Double = 0.0,
  price:Double = 0.0, // always USD precomputed  
  marketCap:Double = 0.0, // always USD 

  categories:Seq[Category],
  locks:Seq[Lock],
  
  holders:Holders,  
  holdersGrowth:Holders,
  uniqueHoldersUp:Holders,
  uniqueHoldersDown:Holders,

  topHolders:Seq[TopHolders],

) extends Ordered[SupplyData] {
  
  def compare (that: SupplyData):Int = {
    if (this.ts == that.ts)
      0
    else if (this.ts > that.ts)
      1
    else
      -1
  }
}

// ATTENTION: this is used for Rangin Ordering ONLY !
object SupplyData {
  def apply(ts:Long) = new SupplyData(ts,null,null,0,0,0,null,null,null,null,null,null,null)
}

// Supply Supply 
case class Supply(
  id: Supply.ID,
  tokenId:Token.ID,  
  name:String = "",  
  history:SortedSet[SupplyData] = SortedSet()  
) 

object Supply {
  type ID = UUID
  def apply(id:String) = UUID(id)
}

