package io.syspulse.haas.circ.serde

import scala.jdk.CollectionConverters._

import scala.util.Random

import io.jvm.uuid._

import io.syspulse.skel.service.JsonCommon
import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.circ
import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation
import io.syspulse.haas.circ.SupplyHolder
import io.syspulse.haas.circ.SupplyBucket

object CirculationSupplyJson extends JsonCommon {
  
  import DefaultJsonProtocol._

  implicit val jf_hl = jsonFormat4(SupplyHolder.apply _)
  implicit val jf_sb = jsonFormat4(SupplyBucket.apply _)
  implicit val jf_c = jsonFormat9(Circulation.apply _)
  implicit val jf_cs = jsonFormat4(CirculationSupply.apply _)
}
