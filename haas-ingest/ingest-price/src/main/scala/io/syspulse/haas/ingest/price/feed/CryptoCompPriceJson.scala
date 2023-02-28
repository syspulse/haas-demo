package io.syspulse.haas.ingest.price.feed

import io.syspulse.skel.service.JsonCommon

import spray.json.DefaultJsonProtocol

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}
import spray.json.NullOptions

import io.syspulse.haas.ingest.price.feed.{CryptoCompPriceData, CryptoCompUSD, CryptoCompPriceFull, CryptoCompPriceTerse}
// with NullOptions only for writing
object CryptoCompPriceJson extends JsonCommon {
  import DefaultJsonProtocol._

  implicit val jf_1 = jsonFormat3(CryptoCompPriceData)
  implicit val jf_2 = jsonFormat1(CryptoCompUSD)
  implicit val jf_3 = jsonFormat1(CryptoCompPriceFull)
  implicit val jf_4 = jsonFormat2(CryptoCompPriceTerse)
}
