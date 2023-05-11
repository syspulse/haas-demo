package io.syspulse.haas.supply.server

import scala.collection.immutable
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import io.syspulse.skel.service.JsonCommon
import io.syspulse.haas.supply.Supply
import io.syspulse.haas.supply.SupplyJson

final case class Supplys(supplys: immutable.Seq[Supply])
final case class SupplyRandomReq()
final case class SupplyActionRes(status: String,id:Option[Supply.ID])
final case class SupplyRes(supplyulation: Option[Supply])

object SupplyProto extends JsonCommon {
  
  import SupplyJson._

  implicit val jf_Supplys = jsonFormat1(Supplys)
  implicit val jf_SupplyRes = jsonFormat1(SupplyRes)
  implicit val jf_ActionRes = jsonFormat2(SupplyActionRes)
  
  implicit val jf_RadnomReq = jsonFormat0(SupplyRandomReq)
  
}