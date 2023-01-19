package io.syspulse.haas.circ.serde

import scala.jdk.CollectionConverters._

import scala.util.Random

import io.jvm.uuid._

import io.syspulse.skel.service.JsonCommon
import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.circ
import io.syspulse.haas.circ._

object CirculationSupplyJson extends JsonCommon {
  
  import DefaultJsonProtocol._

  implicit val jf_sc = jsonFormat2(SupplyCategory.apply _)
  implicit val jf_hl = jsonFormat4(SupplyHolder.apply _)
  implicit val jf_sb = jsonFormat4(SupplyBucket.apply _)
  implicit val jf_c = jsonFormat12(Circulation.apply _)
  implicit val jf_cs = jsonFormat4(CirculationSupply.apply _)
}
