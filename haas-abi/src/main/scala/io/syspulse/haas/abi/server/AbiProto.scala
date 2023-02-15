package io.syspulse.haas.abi.server

import scala.collection.immutable
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import io.syspulse.skel.service.JsonCommon

import io.syspulse.haas.abi._
import io.syspulse.haas.abi.AbiJson

final case class AbiCreateReq(id:String,abis:List[String])
final case class AbiRandomReq()
final case class AbiActionRes(status: String,id:Option[String])
final case class AbiRes(abi: Option[Abi])

object AbiProto extends JsonCommon {
  
  import DefaultJsonProtocol._

  import AbiJson._

  implicit val jf_AbiRes = jsonFormat1(AbiRes)
  implicit val jf_CreateReq = jsonFormat2(AbiCreateReq)
  implicit val jf_ActionRes = jsonFormat2(AbiActionRes)
  
  implicit val jf_RadnomReq = jsonFormat0(AbiRandomReq)
  
}