package io.syspulse.haas.intercept

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.TryValues._
import scala.io.Source

import io.syspulse.haas.core._

import io.syspulse.skel.util.Util
import io.syspulse.haas.ingest.eth.flow.EthDecoder

import io.syspulse.haas.ingest.eth.EthEtlJson

import EthEtlJson._
import spray.json._
import DefaultJsonProtocol._

class InterceptSpec extends AnyWordSpec with Matchers {
  val testDir = this.getClass.getClassLoader.getResource(".").getPath + "../../../"

  "InterceptSpec" should {
    
    "parse USDT Event as ABI " in {

      val e1 = """14,0x224ebde55d7b989f64c3431054af357c3fba52f493dfb855123ecc2f636bcb93,29,0xdac17f958d2ee523a2206206994597c13d831ec7,0x000000000000000000000000000000000000000000000000000000000441c6d1,0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef;0x000000000000000000000000a910f92acdaf488fa6ef02174fb86208ad7722ba;0x000000000000000000000000c5330ae5d9a701a5140c2049de5f4fd08faad292,15611282,1664119679000,0x857c53503bd5751329327830b6a33d1326d2278fcd69b9007ff7db544bdc6482"""
      
      val ed = new EthDecoder[Event] {
        val fmt:JsonFormat[Event] = EthEtlJson._
      }
      
      // h.size should === (1)      
      // h(0).addr should  === ("0x41653c7d61609d856f29355e404f310ec4142cfb")
      // h(0).v should  === (BigInt(1000))
    }

  }

}