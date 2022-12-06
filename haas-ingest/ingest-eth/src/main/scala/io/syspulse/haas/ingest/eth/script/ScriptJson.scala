package io.syspulse.haas.ingest.eth.script

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.skel.service.JsonCommon

object ScriptJson extends JsonCommon with NullOptions {
  implicit val jf_script1 = jsonFormat4(Script.apply _)
}

