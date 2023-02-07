package io.syspulse.haas.ingest.price

import io.syspulse.skel.service.JsonCommon

import spray.json.DefaultJsonProtocol

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}
import spray.json.NullOptions

// with NullOptions only for writing
object ChainlinkPriceJson extends JsonCommon {
  import DefaultJsonProtocol._

  implicit val jf_chp1 = jsonFormat5(ChainlinkPrice)
}
