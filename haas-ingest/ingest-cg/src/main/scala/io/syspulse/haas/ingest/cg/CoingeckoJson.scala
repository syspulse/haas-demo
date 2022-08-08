package io.syspulse.haas.ingest.cg

import io.syspulse.skel.service.JsonCommon

import spray.json.DefaultJsonProtocol

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

object CoingeckoJson extends JsonCommon {
  import DefaultJsonProtocol._

  implicit val jf_CoinInfo = jsonFormat4(CoingeckoCoinInfo)
  implicit val jf_Coin = jsonFormat3(CoingeckoCoin)
  implicit val jf_Coins = jsonFormat1(CoingeckoCoins)
}
