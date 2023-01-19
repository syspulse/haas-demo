package io.syspulse.haas.intercept.script

import scala.collection.immutable
import io.jvm.uuid._

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.skel.service.JsonCommon

final case class Scripts(scripts: immutable.Seq[Script])

object ScriptJson extends JsonCommon with NullOptions {
  implicit val jf_sc1 = jsonFormat4(Script.apply _)
  implicit val jf_sc2 = jsonFormat1(Scripts.apply _)
}

