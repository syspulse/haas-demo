package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util

case class Coins(coins: List[Coin])

case class Coin(id:String,symbol:String,name:String) extends Ingestable

case class MarketCap(usd:Long)

case class MarketData(market_cap:MarketCap)

case class CoinInfo(id:String,symbol:String, name:String,
  contract_address:Option[String], platforms:Map[String,String],
  market_data:MarketData) extends Ingestable

