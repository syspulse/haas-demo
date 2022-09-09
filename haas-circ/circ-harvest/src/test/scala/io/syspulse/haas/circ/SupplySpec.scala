package io.syspulse.haas.circ

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.TryValues._
import scala.io.Source

import io.syspulse.skel.util.Util

class CircSpec extends AnyWordSpec with Matchers {
  //val testDir = this.getClass.getClassLoader.getResource(".").getPath + "../../../"

  "SupplySpec" should {
    "supplyHistory should be 1000000000000000000000000000.0" in {
      // val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
      //   (10861674, "0x0000000000000000000000000000000000000000", -1000000000000000000000000000.0),
      //   (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000000000000000000000000000.0),
      //   (10861766, "0x41653c7d61609d856f29355e404f310ec4142cfb", -2000000000000000000.0),
      //   (10861766, "0xe5737257d9406019768167c26f5c6123864cec1e", 2000000000000000000.0),
      //   (10861777, "0x41653c7d61609d856f29355e404f310ec4142cfb", -8000000000000000000.0),
      //   (10861777, "0xe5737257d9406019768167c26f5c6123864cec1e", 8000000000000000000.0),
      //   (10862390, "0xe5737257d9406019768167c26f5c6123864cec1e", -2000000000000000000.0),
      //   (10862390, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", 2000000000000000000.0),
      //   (10862404, "0xe5737257d9406019768167c26f5c6123864cec1e", -2000000000000000000.0),
      //   (10862404, "0x557847aa724ea004041522ee550e1ae14ced6d7e", 2000000000000000000.0),
      //   (10862407, "0xe5737257d9406019768167c26f5c6123864cec1e", -2000000000000000000.0),
      //   (10862407, "0x672f8518da72910476330b88850d10d83a7c10fe", 2000000000000000000.0)
      // )

      val accountBalanceDeltaCollected: Array[(Int, String, Double)] = Array(
        (10861674, "0x0000000000000000000000000000000000000000", -1000.0),
        (10861674, "0x41653c7d61609d856f29355e404f310ec4142cfb",  1000.0),
        (10861766, "0x41653c7d61609d856f29355e404f310ec4142cfb", -20.0),
        (10861766, "0xe5737257d9406019768167c26f5c6123864cec1e", 20.0),
        (10861777, "0x41653c7d61609d856f29355e404f310ec4142cfb", -80.0),
        (10861777, "0xe5737257d9406019768167c26f5c6123864cec1e", 80.0),
        (10862390, "0xe5737257d9406019768167c26f5c6123864cec1e", -20.0),
        (10862390, "0x1c8bca22fa5f57c3e289ed469bcae62baf63edf7", 20.0),
        (10862404, "0xe5737257d9406019768167c26f5c6123864cec1e", -20.0),
        (10862404, "0x557847aa724ea004041522ee550e1ae14ced6d7e", 20.0),
        (10862407, "0xe5737257d9406019768167c26f5c6123864cec1e", -20.0),
        (10862407, "0x672f8518da72910476330b88850d10d83a7c10fe", 20.0)
      )
      
      val h = Supply.supplyHistory(accountBalanceDeltaCollected)
      info(s"h = ${h}")

      h.size should === (6)
      
      h.head.block should  === (10861674L)
      h.last.block should  === (10862407)

      assert(Math.abs(h.head.totalSupply-1000.0) < 0.1)

      h.head.totalHolders should === (0L)
    }
  }

}