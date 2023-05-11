package io.syspulse.haas.supply

import scala.jdk.CollectionConverters._

import scala.util.Random

import io.jvm.uuid._

import io.syspulse.skel.service.JsonCommon
import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.supply
import io.syspulse.haas.supply._

object SupplyJson extends JsonCommon {
  import JsonCommon._
  import DefaultJsonProtocol._

  implicit val jf_ta = jsonFormat2(TokenAddr.apply _)
  implicit val jf_bv = jsonFormat2(BlockchainValues.apply _)
  implicit val jf_ts = jsonFormat2(TotalSupply.apply _)
  implicit val jf_cs = jsonFormat2(CircSupply.apply _)
  implicit val jf_cat = jsonFormat3(Category.apply _)

  implicit val jf_li = jsonFormat3(LockInfo.apply _)
  implicit val jf_lo = jsonFormat2(Lock.apply _)


  implicit val jf_hbv = jsonFormat2(supply.HolderBlockchainValues.apply _)
  implicit val jf_hl = jsonFormat2(supply.Holders.apply _)
  implicit val jf_tv = jsonFormat2(TV.apply _)
  implicit val jf_tp = jsonFormat3(TopHolders.apply _)

  implicit val jf_sd = jsonFormat13(SupplyData.apply _)
  implicit val jf_s = jsonFormat4(Supply.apply _)
}
