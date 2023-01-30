package io.syspulse.haas.serde

import scala.jdk.CollectionConverters._

import scala.util.Random

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.core.Event

object EventJson extends DefaultJsonProtocol {
  
  implicit val jf_event = jsonFormat6(Event.apply _)
}
