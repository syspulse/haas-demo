package io.syspulse.haas.intercept

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.skel.service.JsonCommon

object InterceptionJson extends JsonCommon with NullOptions {
  
  implicit val jf_2 = jsonFormat6(InterceptionAlarm.apply _)
  implicit val jf_1 = jsonFormat11(Interception.apply _)
  
}

