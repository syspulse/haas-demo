package io.syspulse.haas.circ

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import io.jvm.uuid._
import scala.math.BigInt
import scala.io.Source

import io.syspulse.haas.core._

case class TotalSupply(totalContract:Double,totalHolders:Double)

case class BlockTransfer(addr:String,value:Double)
case class BlockSupply(block:Long,totalHolders:Long,totalSupply:Double)

object Supply {
  def foldBlockTransfer(bts: Array[BlockTransfer]):Array[BlockTransfer] = { 
    if(bts.size == 2) return bts

    bts.groupBy(_.addr).map{ case(addr,bts) => { if(bts.size==1) bts.head else {val valueAggr = bts.foldLeft(0.0)( _ + _.value); BlockTransfer(addr,valueAggr) }} }.toArray
  }

  def fromFile(file:String):Array[(Int, String, Double)] = {
    Source.fromFile(file)
      .getLines()
      .flatMap(s => s.split(",").toList match {
        case erc20 :: from_addr :: to_addr :: value :: _ :: _ :: block :: Nil => Some(
          List(
            (block.toInt,from_addr,- BigInt(value).doubleValue),
            (block.toInt,to_addr,BigInt(value).doubleValue)
          )
        )
        case _ => None
      }).toArray.flatten.sortBy(_._1)
  }

  // Accepts: collected (block,address,balance)
  // the function is optimized for very large sets processing 
  // does not use Functional style, heavily imperative to keep code sane and be memory efficient
  // uses mutable collections
  // uses tracking vars
  def supplyHistory(accountBalanceDeltaCollected: Array[(Int, String, Double)]):List[BlockSupply] = {
    var supply: List[BlockSupply] = List()
    var lastBlock: Long = accountBalanceDeltaCollected.head._1
    // this is optimization to track holders to avoid historyBalance tranversal to see which balances went to 0
    var totalHolders = 0L
    // total supply
    var totalSupply = 0.0
    // imbalance
    var totalSupplyImbalance = 0.0

    // history balances tracker with each block traversal (to calculate # of holders at each block)
    val historyHolders = mutable.HashMap[String,Double]()
    
    for(i <- 0 to accountBalanceDeltaCollected.size - 1) {
      
      val a = accountBalanceDeltaCollected(i)
      val block = a._1
      val addr = a._2
      val value = a._3

      println(s"(lastBlock=${lastBlock},totalHolders=${totalHolders},totalSupply=${totalSupply}): $i: ${block},${addr},${value}")

      if(block != lastBlock) {
        supply = supply :+ BlockSupply(lastBlock,totalHolders,totalSupply)
        println(s"(lastBlock=${lastBlock},totalHolders=${totalHolders},totalSupply=${totalSupply}): ${supply}")
        
        lastBlock = block
      }

      addr match {
        case "0x0000000000000000000000000000000000000000" => {
          // mint or burn
          totalSupply = totalSupply + (-value)        
        }
        case _ => {
          val balance = historyHolders.get(addr)
          if(balance.isDefined) {
            // existing address found, check balance goes to 0

            val newBalance = balance.get + value
            if(DoubleValue.isZero(newBalance)) {
              // remove as a holder from history
              totalHolders = totalHolders - 1
            }

            // save with a new balance
            historyHolders.put(addr,newBalance)
          } else {
            // save first occurance of address
            historyHolders.put(addr,value)
            // add new holder to history
            totalHolders = totalHolders + 1
          }          
        }
      }      
      

      // total change should always be zeroed for BlockTransfers within the same block (transfer is atomic)    
      totalSupplyImbalance = totalSupplyImbalance + value      
    }

    supply = supply :+ BlockSupply(lastBlock,totalHolders,totalSupply)
    println(s"(lastBlock=${lastBlock},totalHolders=${totalHolders},totalSupply=${totalSupply}): ${supply}")
        
    supply
  }
}