package io.syspulse.haas.ingest.eth.server

import scala.collection.immutable
import io.jvm.uuid._

import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import io.syspulse.skel.service.JsonCommon
import io.syspulse.haas.ingest.eth.intercept.Interception
import io.syspulse.haas.ingest.eth.intercept.InterceptionJson
import io.syspulse.haas.ingest.eth.script._

final case class Interceptions(tokens: immutable.Seq[Interception])
final case class InterceptionCreateReq(id:Option[Interception.ID],name:String, script:String, alarm:List[String], uid:Option[UUID] = None)
final case class InterceptionActionRes(status: String,id:Option[String])
final case class InterceptionRes(token: Option[Interception])

object InterceptionProto extends JsonCommon {
  
  import InterceptionJson._

  implicit val jf_Intercepts = jsonFormat1(Interceptions)
  implicit val jf_InterceptRes = jsonFormat1(InterceptionRes)
  implicit val jf_CreateReq = jsonFormat5(InterceptionCreateReq)
  implicit val jf_ActionRes = jsonFormat2(InterceptionActionRes)
  
}