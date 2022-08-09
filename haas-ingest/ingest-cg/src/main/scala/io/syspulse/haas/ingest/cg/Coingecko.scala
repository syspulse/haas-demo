package io.syspulse.haas.ingest.cg

import scala.jdk.CollectionConverters._

import io.syspulse.skel.ingest.Ingestable
import io.syspulse.skel.util.Util

case class CoingeckoCoins(coins: List[CoingeckoCoin])

case class CoingeckoCoin(id:String,symbol:String,name:String) extends Ingestable {
  def toLog = s""
  def toSimpleLog = s""
}

case class MarketCap(usd:Long)

case class MarketData(market_cap:MarketCap)

case class CoingeckoCoinInfo(id:String,symbol:String, name:String,
  contract_address:Option[String], platforms:Map[String,String],
  market_data:MarketData) extends Ingestable {
  def toLog = s""
  def toSimpleLog = s""
}

