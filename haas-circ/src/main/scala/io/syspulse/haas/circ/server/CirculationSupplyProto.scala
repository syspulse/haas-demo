package io.syspulse.haas.circ.server

import scala.collection.immutable
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import io.syspulse.skel.service.JsonCommon
import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation
import io.syspulse.haas.circ.serde.CirculationSupplyJson

final case class CirculationSupplys(circulations: immutable.Seq[CirculationSupply])
final case class CirculationSupplyRandomReq()
final case class CirculationSupplyActionRes(status: String,id:Option[CirculationSupply.ID])
final case class CirculationSupplyRes(circulation: Option[CirculationSupply])

object CirculationSupplyProto extends JsonCommon {
  
  import CirculationSupplyJson._

  implicit val jf_CirculationSupplys = jsonFormat1(CirculationSupplys)
  implicit val jf_CirculationSupplyRes = jsonFormat1(CirculationSupplyRes)
  implicit val jf_ActionRes = jsonFormat2(CirculationSupplyActionRes)
  
  implicit val jf_RadnomReq = jsonFormat0(CirculationSupplyRandomReq)
  
}