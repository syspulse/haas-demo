package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util

case class Coins(coins: List[Coin])

case class Coin(id:String,symbol:String,name:String) extends Ingestable

case class MarketCap(usd:Long)

case class MarketData(market_cap:MarketCap)

case class Links(homepage:List[Option[String]])
case class Image(thumb:String,small:String,large:String)

case class CoinInfo(id:String,symbol:String, name:String,
  contract_address:Option[String], platforms:Map[String,Option[String]] = Map(),
  categories:List[String] = List(),
  links:Links = Links(List()),
  image:Image = Image("","",""),
//  market_data:MarketData,
  last_updated:Option[String] = None) extends Ingestable

