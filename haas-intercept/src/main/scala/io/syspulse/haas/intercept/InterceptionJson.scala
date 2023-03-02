package io.syspulse.haas.intercept

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.skel.service.JsonCommon

object InterceptionJson extends JsonCommon with NullOptions {
  
  implicit val jf_2 = jsonFormat7(InterceptionAlarm.apply _)
  implicit val jf_1 = jsonFormat12(Interception.apply _)
  
}

