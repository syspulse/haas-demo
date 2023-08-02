package io.syspulse.haas.holder.store

import scala.jdk.CollectionConverters._
import io.syspulse.skel.Ingestable
import scala.collection.SortedSet

import spray.json.DefaultJsonProtocol
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}
import io.syspulse.skel.service.JsonCommon

// Json Datalake format
case class Holders(  
  token:String,
  ts:Long,
  holders:Seq[(String,BigInt)] = Seq(),
)


object HoldersFormatJson extends JsonCommon {  
  implicit val jf_HoldersJson = jsonFormat3(Holders)
}

