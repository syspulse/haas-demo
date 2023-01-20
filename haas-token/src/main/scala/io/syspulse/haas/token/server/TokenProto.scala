package io.syspulse.haas.token.server

import scala.collection.immutable
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import io.syspulse.skel.service.JsonCommon
import io.syspulse.haas.core.Token
import io.syspulse.haas.serde.TokenJson

final case class Tokens(tokens: immutable.Seq[Token], total:Option[Long] = None)
final case class TokenCreateReq(id:String,symbol: String, name:String, contractAddress: Option[String] = None)
final case class TokenRandomReq()
final case class TokenActionRes(status: String,id:Option[String])
final case class TokenRes(token: Option[Token])

object TokenProto extends JsonCommon {
  
  import TokenJson._

  implicit val jf_Tokens = jsonFormat2(Tokens)
  implicit val jf_TokenRes = jsonFormat1(TokenRes)
  implicit val jf_CreateReq = jsonFormat4(TokenCreateReq)
  implicit val jf_ActionRes = jsonFormat2(TokenActionRes)
  
  implicit val jf_RadnomReq = jsonFormat0(TokenRandomReq)
  
}