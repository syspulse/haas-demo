package io.syspulse.haas.ingest.gecko

import io.syspulse.skel.service.JsonCommon

import spray.json.DefaultJsonProtocol

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

object CoingeckoJson extends JsonCommon {
  import DefaultJsonProtocol._

  implicit val jf_mk = jsonFormat1(MarketCap)
  implicit val jf_md = jsonFormat1(MarketData)
  implicit val jf_ln = jsonFormat1(Links)
  implicit val jf_im = jsonFormat3(Image)
  implicit val jf_CoinInfo = jsonFormat8(CoinInfo)
  implicit val jf_Coin = jsonFormat3(Coin)
  implicit val jf_Coins = jsonFormat1(Coins)
}
