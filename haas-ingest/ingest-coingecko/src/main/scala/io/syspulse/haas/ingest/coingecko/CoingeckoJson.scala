package io.syspulse.haas.ingest.coingecko

import io.syspulse.skel.service.JsonCommon

import spray.json.DefaultJsonProtocol
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, deserializationError}
import spray.json.NullOptions

// with NullOptions only for writing
object CoinGeckoTokenJson extends JsonCommon {
  implicit val jf_MarketCap = jsonFormat1(MarketCap)
  implicit val jf_MarketData = jsonFormat1(MarketData)
  implicit val jf_Links = jsonFormat1(Links)
  implicit val jf_Image = jsonFormat3(Image)
  implicit val jf_DetailPlatform = jsonFormat2(DetailPlatform)
  implicit val jf_CoinInfo = jsonFormat11(CoinInfo)
  implicit val jf_Coin = jsonFormat3(Coin)
  implicit val jf_Coins = jsonFormat1(Coins)
}

// with NullOptions only for writing
object CoinGeckoPriceJson extends JsonCommon {
  implicit val jf_Price = jsonFormat2(CoinGeckoPrice)
}