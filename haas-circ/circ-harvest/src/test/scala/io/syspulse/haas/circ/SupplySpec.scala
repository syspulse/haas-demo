package io.syspulse.haas.circ

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.TryValues._
import scala.io.Source

import io.syspulse.haas.core._

import io.syspulse.skel.util.Util
import java.math.BigInteger

class CircSpec extends AnyWordSpec with Matchers {
  val testDir = this.getClass.getClassLoader.getResource(".").getPath + "../../../"

  "SupplySpec" should {
    
    "supplyHistory should be 1000.0 and holders = 1" in {

      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000.0),        
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      
      h.size should === (1)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861674)

      DoubleValue.==(h.head.totalSupply,1000.0) should === (true)
      DoubleValue.==(h.last.totalSupply,1000.0) should === (true)

      h.head.totalHolders should === (1)
      h.last.totalHolders should === (1)
    }

    "supplyHistory should be 2000.0 with 2 mints and holders = 2" in {

      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000.0),

        (10861675, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861675, "0xe5737257d9406019768167c26f5c6123864cec1e",  1000.0),
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      
      h.size should === (2)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861675)

      DoubleValue.==(h.head.totalSupply,1000.0) should === (true)
      DoubleValue.==(h.last.totalSupply,2000.0) should === (true)

      h.head.totalHolders should === (1)
      h.last.totalHolders should === (2)
    }

    "supplyHistory should be 1500.0 with 2 mints and 1 burn and holders = 2" in {

      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000.0),

        (10861675, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861675, "0xe5737257d9406019768167c26f5c6123864cec1e",  1000.0),

        (10861676, "0x41653c7d61609d856f29355e404f310ec4142cfb",  -500.0),
        (10861676, "0x0000000000000000000000000000000000000000",  500.0),        
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      
      h.size should === (3)      
      h(0).block should  === (10861674)
      h(2).block should  === (10861676)

      DoubleValue.==(h(0).totalSupply,1000.0) should === (true)
      DoubleValue.==(h(1).totalSupply,2000.0) should === (true)
      DoubleValue.==(h(2).totalSupply,1500.0) should === (true)

      h(0).totalHolders should === (1)
      h(1).totalHolders should === (2)
      h(2).totalHolders should === (2)
    }
    

    "supplyHistory should be 1000.0 and holders = 2 for multiple transfers from the same account in block" in {

      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", -20.0),
        (10861674, "0xe5737257d9406019768167c26f5c6123864cec1e", 20.0),
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (1)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861674)

      DoubleValue.==(h.head.totalSupply,1000.0) should === (true)
      DoubleValue.==(h.last.totalSupply,1000.0) should === (true)

      h.head.totalHolders should === (2)
      h.last.totalHolders should === (2)
    }

    "supplyHistory should be 1000.0 and holders = 3 for multiple transfers from the same account in block" in {

      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", -20.0),
        (10861674, "0xe5737257d9406019768167c26f5c6123864cec1e", 20.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", -80.0),
        (10861674, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", 80.0),
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (1)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861674)

      DoubleValue.==(h.head.totalSupply,1000.0) should === (true)
      DoubleValue.==(h.last.totalSupply,1000.0) should === (true)

      h.head.totalHolders should === (3)
      h.last.totalHolders should === (3)
    }

    "supplyHistory should be 1000.0 and holders = 2 when 1 accounts go to Zero" in {

      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", -200.0),
        (10861674, "0xe5737257d9406019768167c26f5c6123864cec1e", 200.0),
        (10861777, "0x41653c7d61609d856f29355e404f310ec4142cfb", -800.0),
        (10861777, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", 800.0),
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (2)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861777)

      DoubleValue.==(h.head.totalSupply,1000.0) should === (true)
      DoubleValue.==(h.last.totalSupply,1000.0) should === (true)

      h.head.totalHolders should === (2)
      h.last.totalHolders should === (2)
    }

    "supplyHistory should be 1000.0 and holders = 1 when 2 accounts go to Zero" in {

      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", -200.0),
        (10861674, "0xe5737257d9406019768167c26f5c6123864cec1e", 200.0),        
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", -500.0),
        (10861674, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", 500.0),

        (10861675, "0x41653c7d61609d856f29355e404f310ec4142cfb", -300.0),
        (10861675, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", 300.0),

        (10861777, "0xe5737257d9406019768167c26f5c6123864cec1e", -200.0),
        (10861777, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", 200.0),
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (3)
      h.head.block should  === (10861674)
      h.last.block should  === (10861777)

      DoubleValue.==(h.head.totalSupply,1000.0) should === (true)
      DoubleValue.==(h.last.totalSupply,1000.0) should === (true)

      h.head.totalHolders should === (3)
      h.last.totalHolders should === (1)
    }


    "supplyHistory for UNI 1000 blocks should be 1000000000000000000000000000 and holders = 5" in {
      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000000000000000000000000000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000000000000000000000000000.0),
        (10861766, "0x41653c7d61609d856f29355e404f310ec4142cfb", -2000000000000000000.0),
        (10861766, "0xe5737257d9406019768167c26f5c6123864cec1e", 2000000000000000000.0),
        (10861777, "0x41653c7d61609d856f29355e404f310ec4142cfb", -8000000000000000000.0),
        (10861777, "0xe5737257d9406019768167c26f5c6123864cec1e", 8000000000000000000.0),
        (10862390, "0xe5737257d9406019768167c26f5c6123864cec1e", -2000000000000000000.0),
        (10862390, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", 2000000000000000000.0),
        (10862404, "0xe5737257d9406019768167c26f5c6123864cec1e", -2000000000000000000.0),
        (10862404, "0x557847aa724ea004041522ee550e1ae14ced6d7e", 2000000000000000000.0),
        (10862407, "0xe5737257d9406019768167c26f5c6123864cec1e", -2000000000000000000.0),
        (10862407, "0x672f8518da72910476330b88850d10d83a7c10fe", 2000000000000000000.0)
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (6)
      
      h.head.block should  === (10861674)
      h.last.block should  === (10862407)

      DoubleValue.==(h.head.totalSupply,1000000000000000000000000000.0) should === (true)
      DoubleValue.==(h.last.totalSupply,1000000000000000000000000000.0) should === (true)

      h.head.totalHolders should === (1)
      h.last.totalHolders should === (5)
    }  

    "supplyHistory for UNI 5000 blocks should be 1000000000000000000000000000 and holders = 6" in {
      
      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Supply.fromFile(testDir + "UNI-5000.csv")
                
      info(s"${accountBalanceDeltaCollected}")
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (7)
      
      h.head.block should  === (10861674)
      h.last.block should  === (10863690)

      DoubleValue.==(h.head.totalSupply,1000000000000000000000000000.0) should === (true)
      DoubleValue.==(h.last.totalSupply,1000000000000000000000000000.0) should === (true)

      h.head.totalHolders should === (1)
      h.last.totalHolders should === (5)
    }  
  }

}