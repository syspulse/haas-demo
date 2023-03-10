package io.syspulse.haas.intercept.server

import scala.collection.immutable
import io.jvm.uuid._

import spray.json._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives

import io.syspulse.skel.service.JsonCommon
import io.syspulse.haas.intercept.script._

final case class Scripts(scripts: immutable.Seq[Script])

final case class ScriptUpdateReq(
  id:Option[Script.ID],
  name:Option[String], 
  desc:Option[String], 
  src:Option[String]
)

object ScriptJson extends JsonCommon with NullOptions {
  implicit val jf_script = jsonFormat9(Script.apply _)
  implicit val jf_scripts = jsonFormat1(Scripts.apply _)
  implicit val jf_scriptUpdate = jsonFormat4(ScriptUpdateReq.apply _)
}

