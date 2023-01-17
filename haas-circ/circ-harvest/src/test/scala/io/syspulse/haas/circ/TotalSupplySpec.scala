package io.syspulse.haas.circ

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.TryValues._
import scala.io.Source

import io.syspulse.haas.core._

import io.syspulse.skel.util.Util
import java.math.BigInteger

class TotalSupplySpec extends AnyWordSpec with Matchers {
  val testDir = this.getClass.getClassLoader.getResource(".").getPath + "../../../"

  "TotalSupplySpec" should {
    
    "history should be BigInt(1000) and holders = 1" in {

      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt(1000)),        
      )
      
      val h = Supply.history(accountBalanceDeltaCollected)
      
      h.size should === (1)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861674)

      h.head.totalSupply should === (BigInt(1000))
      h.last.totalSupply should === (BigInt(1000))

      h.head.totalHolders should === (1)
      h.last.totalHolders should === (1)
    }

    "history should be 2000.0 with 2 mints and holders = 2" in {

      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt(1000)),

        (10861675, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861675, "0xe5737257d9406019768167c26f5c6123864cec1e",  BigInt(1000)),
      )
      
      val h = Supply.history(accountBalanceDeltaCollected)
      
      h.size should === (2)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861675)

      h.head.totalSupply should === (BigInt(1000))
      h.last.totalSupply should === (BigInt(2000))

      h.head.totalHolders should === (1)
      h.last.totalHolders should === (2)
    }

    "history should be 1500.0 with 2 mints and 1 burn and holders = 2" in {

      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt(1000)),

        (10861675, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861675, "0xe5737257d9406019768167c26f5c6123864cec1e",  BigInt(1000)),

        (10861676, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt(-500)),
        (10861676, "0x0000000000000000000000000000000000000000",  BigInt(500)),        
      )
      
      val h = Supply.history(accountBalanceDeltaCollected)
      
      h.size should === (3)      
      h(0).block should  === (10861674)
      h(2).block should  === (10861676)

      h(0).totalSupply should === (BigInt(1000))
      h(1).totalSupply should === (BigInt(2000))
      h(2).totalSupply should === (BigInt(1500))

      h(0).totalHolders should === (1)
      h(1).totalHolders should === (2)
      h(2).totalHolders should === (2)
    }
    

    "history should be BigInt(1000) and holders = 2 for multiple transfers from the same account in block" in {

      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt(1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(-20)),
        (10861674, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt(20)),
      )
      
      val h = Supply.history(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (1)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861674)

      h.head.totalSupply should === (BigInt(1000))
      h.last.totalSupply should === (BigInt(1000))

      h.head.totalHolders should === (2)
      h.last.totalHolders should === (2)
    }

    "history should be BigInt(1000) and holders = 3 for multiple transfers from the same account in block" in {

      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(-20)),
        (10861674, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt(20)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(-80)),
        (10861674, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", BigInt(80)),
      )
      
      val h = Supply.history(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (1)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861674)

      h.head.totalSupply should === (BigInt(1000))
      h.last.totalSupply should === (BigInt(1000))

      h.head.totalHolders should === (3)
      h.last.totalHolders should === (3)
    }

    "history should be BigInt(1000) and holders = 2 when 1 accounts go to Zero" in {

      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt(1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(-200)),
        (10861674, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt(200)),
        (10861777, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(-800)),
        (10861777, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", BigInt(800)),
      )
      
      val h = Supply.history(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (2)      
      h.head.block should  === (10861674)
      h.last.block should  === (10861777)

      h.head.totalSupply should === (BigInt(1000))
      h.last.totalSupply should === (BigInt(1000))

      h.head.totalHolders should === (2)
      h.last.totalHolders should === (2)
    }

    "history should be BigInt(1000) and holders = 1 when 2 accounts go to Zero" in {

      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt(1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(-200)),
        (10861674, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt(200)),        
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(-500)),
        (10861674, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", BigInt(500)),

        (10861675, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt(-300)),
        (10861675, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", BigInt(300)),

        (10861777, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt(-200)),
        (10861777, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", BigInt(200)),
      )
      
      val h = Supply.history(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (3)
      h.head.block should  === (10861674)
      h.last.block should  === (10861777)

      h.head.totalSupply should === (BigInt(1000))
      h.last.totalSupply should === (BigInt(1000))

      h.head.totalHolders should === (3)
      h.last.totalHolders should === (1)
    }


    "history for UNI 1000 blocks should be 1000000000000000000000000000 and holders = 6" in {
      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt("-1000000000000000000000000000")),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt("1000000000000000000000000000")),
        (10861766, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt("-2000000000000000000")),
        (10861766, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("2000000000000000000")),
        (10861777, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt("-8000000000000000000")),
        (10861777, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("8000000000000000000")),
        (10862390, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("-2000000000000000000")),
        (10862390, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", BigInt("2000000000000000000")),
        (10862404, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("-2000000000000000000")),
        (10862404, "0x557847aa724ea004041522ee550e1ae14ced6d7e", BigInt("2000000000000000000")),
        (10862407, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("-2000000000000000000")),
        (10862407, "0x672f8518da72910476330b88850d10d83a7c10fe", BigInt("2000000000000000000"))
      )
      
      val h = Supply.history(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (6)
      
      h.head.block should  === (10861674)
      h.last.block should  === (10862407)

      h.head.totalSupply should === (BigInt("1000000000000000000000000000"))
      h.last.totalSupply should === (BigInt("1000000000000000000000000000"))

      h.head.totalHolders should === (1)
      h.last.totalHolders should === (5)
    }  

    "history for UNI 5000 blocks should be 1000000000000000000000000000 and holders = 6" in {
      
      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Supply.fromFile(testDir + "UNI-5000.csv")
                
      info(s"${accountBalanceDeltaCollected.toList}")
      
      val h = Supply.history(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (7)
      
      h.head.block should  === (10861674)
      h.last.block should  === (10863690)

      h.head.totalSupply should === (BigInt("1000000000000000000000000000"))
      h.last.totalSupply should === (BigInt("1000000000000000000000000000"))

      h.head.totalHolders should === (1)
      h.last.totalHolders should === (5)
    }  
  }

}