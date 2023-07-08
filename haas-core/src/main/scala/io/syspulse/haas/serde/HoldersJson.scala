package io.syspulse.haas.serde

import scala.jdk.CollectionConverters._

import scala.util.Random

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.core.{Holder,Holders}

object HoldersJson extends DefaultJsonProtocol {
  implicit val jf_holder = jsonFormat2(Holder.apply _)
  implicit val jf_holders = jsonFormat4(Holders.apply _)
}
