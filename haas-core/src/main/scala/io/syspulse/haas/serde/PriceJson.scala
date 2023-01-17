package io.syspulse.haas.serde

import scala.jdk.CollectionConverters._

import scala.util.Random

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.core.Price

object PriceJson extends DefaultJsonProtocol {
  
  implicit val jf_price = jsonFormat5(Price.apply _)
}
