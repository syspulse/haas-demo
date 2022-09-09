package io.syspulse.haas.circ

import scala.jdk.CollectionConverters._
import io.jvm.uuid._
import scala.math.BigInt


case class TotalSupply(totalContract:Double,totalHolders:Double)

case class BlockTransfer(addr:String,value:Double)
case class BlockSupply(block:Long,totalHolders:Long,totalSupply:Double)

object Supply {
  def foldBlockTransfer(bts: Array[BlockTransfer]):Array[BlockTransfer] = { 
    if(bts.size == 2) return bts

    bts.groupBy(_.addr).map{ case(addr,bts) => { if(bts.size==1) bts.head else {val valueAggr = bts.foldLeft(0.0)( _ + _.value); BlockTransfer(addr,valueAggr) }} }.toArray
  }

  def supplyHistory(accountBalanceDeltaCollected: Array[(Int, String, Double)]):List[BlockSupply] = {
    var supply: List[BlockSupply] = List()
    var lastBlock: Long = accountBalanceDeltaCollected(0)._1
    var totalHolders = 0L
    var totalSupply = 0.0

    for(i <- 0 to accountBalanceDeltaCollected.size - 1) {
      val a = accountBalanceDeltaCollected(i)
      val block = a._1
      val addr = a._2
      val value = a._3

      addr match {
        case "0x0000000000000000000000000000000000000000" => totalSupply = Math.abs(value)
        case _ => 
      }
      
      if(block != lastBlock || i == accountBalanceDeltaCollected.size - 1) {
        supply = supply :+ BlockSupply(lastBlock,totalHolders,totalSupply) 
        
        lastBlock = block
      }

      // total change should always be zeroed for BlockTransfers within the same block (transfer is atomic)
      totalSupply = totalSupply + value
    }

    supply
  }
}