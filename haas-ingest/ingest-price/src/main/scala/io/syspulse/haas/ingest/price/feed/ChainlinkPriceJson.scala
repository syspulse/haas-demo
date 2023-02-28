package io.syspulse.haas.ingest.price.feed

import io.syspulse.skel.service.JsonCommon

import spray.json.DefaultJsonProtocol

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}
import spray.json.NullOptions

import io.syspulse.haas.ingest.price.feed.ChainlinkPrice
// with NullOptions only for writing
object ChainlinkPriceJson extends JsonCommon {
  import DefaultJsonProtocol._

  implicit val jf_chp1 = jsonFormat5(ChainlinkPrice)
}
