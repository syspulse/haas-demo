package io.syspulse.haas.circ

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.TryValues._
import scala.io.Source

import io.syspulse.haas.core._

import io.syspulse.skel.util.Util
import java.math.BigInteger

class HoldersSpec extends AnyWordSpec with Matchers {
  val testDir = this.getClass.getClassLoader.getResource(".").getPath + "../../../"

  "HoldersSpec" should {
    
    "holders should be 1" in {

      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt(-1000)),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt(1000)),
      )
      
      val h = Supply.holders(accountBalanceDeltaCollected)
      
      h.size should === (1)      
      h(0).addr should  === ("0x41653c7d61609d856f29355e404f310ec4142cfb")
      h(0).v should  === (BigInt(1000))
    }

    "holders for UNI 1000 blocks should be 5 in sorted order" in {
      val accountBalanceDeltaCollected: Array[(Int, String, BigInt)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", BigInt("-100")),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  BigInt("100")),
        (10861766, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt("-2")),
        (10861766, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("2")),
        (10861777, "0x41653c7d61609d856f29355e404f310ec4142cfb", BigInt("-8")),
        (10861777, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("8")),
        (10862390, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("-2")),
        (10862390, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", BigInt("2")),
        (10862404, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("-2")),
        (10862404, "0x557847aa724ea004041522ee550e1ae14ced6d7e", BigInt("2")),
        (10862407, "0xe5737257d9406019768167c26f5c6123864cec1e", BigInt("-1")),
        (10862407, "0x672f8518da72910476330b88850d10d83a7c10fe", BigInt("1"))
      )
      
      val h = Supply.holders(accountBalanceDeltaCollected,5)
      
      h.size should === (5)

      h(0).addr should  === ("0x41653c7d61609d856f29355e404f310ec4142cfb")
      h(0).v should  === (BigInt(90))
      
      h(4).addr should  === ("0x672f8518da72910476330b88850d10d83a7c10fe")
      h(4).v should  === (BigInt(1))
    }  

    
  }

}