package io.syspulse.haas.intercept.server

import scala.collection.immutable
import io.jvm.uuid._

import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import io.syspulse.skel.service.JsonCommon
import io.syspulse.haas.intercept.Interception
import io.syspulse.haas.intercept.InterceptionJson
import io.syspulse.haas.intercept.script._
import io.syspulse.haas.core.Blockchain

final case class Interceptions(interceptions: immutable.Seq[Interception])
final case class InterceptionCreateReq(
  id:Option[Interception.ID],
  name:String, 
  script:String, 
  alarm:List[String], 
  uid:Option[UUID] = None, 
  bid:Option[Blockchain.ID] = None,
  entity:Option[String] = Some("tx"), 
  abi:Option[String] = None,
  contract:Option[String] = None)

final case class InterceptionActionRes(status: String,id:Option[String])
final case class InterceptionRes(interception: Option[Interception])
final case class InterceptionCommandReq(command:String,id:Option[Interception.ID]=None)

object InterceptionProto extends JsonCommon {
  import InterceptionJson._

  implicit val jf_ix = jsonFormat1(Interceptions)
  implicit val jf_ixs = jsonFormat1(InterceptionRes)
  implicit val jf_CreateReq = jsonFormat9(InterceptionCreateReq)
  implicit val jf_ActionRes = jsonFormat2(InterceptionActionRes)
  implicit val jf_5 = jsonFormat2(InterceptionCommandReq)
}