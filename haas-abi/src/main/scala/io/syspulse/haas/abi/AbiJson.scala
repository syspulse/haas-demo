package io.syspulse.haas.abi

import scala.jdk.CollectionConverters._

import scala.util.Random

import spray.json._
import DefaultJsonProtocol._

object AbiJson extends  DefaultJsonProtocol {
  
  implicit val jf_tag = jsonFormat5(Abi.apply _)
  implicit val jf_tags = jsonFormat2(Abis.apply _)
}
