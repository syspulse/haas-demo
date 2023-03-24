package io.syspulse.haas.holder.server

import scala.collection.immutable
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import io.syspulse.skel.service.JsonCommon
import io.syspulse.haas.core.Holders
import io.syspulse.haas.serde.HoldersJson

final case class Holderss(holders: immutable.Seq[Holders], total:Option[Long] = None)
final case class HolderCreateReq(id:String, symbol: String, name:String, 
                                cat:Option[List[String]] = None, icon:Option[String]=None, decimals:Option[Int], 
                                contracts: Option[Map[String,String]] = None)
final case class HolderUpdateReq(id:Option[String] = None, symbol: Option[String] = None, name:Option[String] = None, 
                                cat:Option[List[String]] = None, icon:Option[String]=None, decimals:Option[Int] = None, 
                                contracts: Option[Map[String,String]] = None)
final case class HolderRandomReq()
final case class HolderActionRes(status: String,id:Option[String])
final case class HolderRes(holders: Option[Holders])

object HolderProto extends JsonCommon {
  
  import HoldersJson._

  implicit val jf_Holders = jsonFormat2(Holderss)
  implicit val jf_HolderRes = jsonFormat1(HolderRes)
  implicit val jf_CreateReq = jsonFormat7(HolderCreateReq)
  implicit val jf_UpdateReq = jsonFormat7(HolderUpdateReq)
  implicit val jf_ActionRes = jsonFormat2(HolderActionRes)
  
  implicit val jf_RadnomReq = jsonFormat0(HolderRandomReq)
  
}