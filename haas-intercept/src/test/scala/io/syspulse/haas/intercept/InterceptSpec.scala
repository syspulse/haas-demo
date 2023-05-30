package io.syspulse.haas.intercept

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.TryValues._
import scala.io.Source

import io.syspulse.haas.core._

import io.syspulse.skel.util.Util
import io.syspulse.haas.ingest.eth.flow.etl.EthDecoder

import io.syspulse.haas.ingest.eth.EthEtlJson

import EthEtlJson._
import spray.json._
import DefaultJsonProtocol._
import io.syspulse.haas.ingest.eth.EthLog
import io.syspulse.skel.crypto.eth.abi.AbiStoreDir
import io.syspulse.skel.crypto.eth.abi.AbiStoreSigFuncResolver
import io.syspulse.skel.crypto.eth.abi.AbiStoreSignaturesMem
import io.syspulse.skel.crypto.eth.abi.AbiStoreSignatures
import io.syspulse.skel.crypto.eth.abi.SignatureStoreMem
import io.syspulse.skel.crypto.eth.abi.FuncSignature
import io.syspulse.skel.crypto.eth.abi.EventSignature


class Decoder[T](implicit val fmt:JsonFormat[T]) extends EthDecoder[T] {

}

class InterceptSpec extends AnyWordSpec with Matchers {
  val testDir = this.getClass.getClassLoader.getResource(".").getPath + "../../../"

  val abi = new AbiStoreDir(s"${testDir}/store/abi",
      new SignatureStoreMem[FuncSignature](),
      new SignatureStoreMem[EventSignature]()) with AbiStoreSignaturesMem
  
  abi.load()

  "InterceptSpec" should {
    
    "parse USDT Event as ABI " in {
      val decoder = new Decoder[EthLog]
      val d1 = """{"type": "log", 
        "log_index": 14, 
        "transaction_hash": "0x224ebde55d7b989f64c3431054af357c3fba52f493dfb855123ecc2f636bcb93", 
        "transaction_index": 29, 
        "address": "0xdac17f958d2ee523a2206206994597c13d831ec7", 
        "data": "0x000000000000000000000000000000000000000000000000000000000441c6d1", 
        "topics": [
          "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef", 
          "0x000000000000000000000000a910f92acdaf488fa6ef02174fb86208ad7722ba", 
          "0x000000000000000000000000c5330ae5d9a701a5140c2049de5f4fd08faad292"
        ], 
        "block_number": 15611282, 
        "block_timestamp": 1664119679, 
        "block_hash": "0x857c53503bd5751329327830b6a33d1326d2278fcd69b9007ff7db544bdc6482", 
        "item_id": "log_0x224ebde55d7b989f64c3431054af357c3fba52f493dfb855123ecc2f636bcb93_14", 
        "item_timestamp": "2022-09-25T15:27:59Z"}"""

      val t = decoder.parseEventLog(d1)
      info(s"${t}")
      
      t.head.topics.size should === (3)
      
      val r = abi.decodeInput(t.head.address,t.head.topics :+ t.head.data,"event")
      info(s"$r")

      // h(0).addr should  === ("0x41653c7d61609d856f29355e404f310ec4142cfb")
      // h(0).v should  === (BigInt(1000))
    }

  }

}