package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util
import io.syspulse.haas.core.Token
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.core.TokenBlockchain

case class Coins(coins: List[Coin])

case class Coin(id:String,symbol:String,name:String) extends Ingestable

case class MarketCap(usd:Long)

case class MarketData(market_cap:MarketCap)

case class Links(homepage:List[Option[String]])
case class Image(thumb:String,small:String,large:String)

case class DetailPlatform(decimal_place:Option[Int],contract_address:String)

case class CoinInfo(id:String,symbol:String, name:String,  
  contract_address:Option[String], platforms:Map[String,Option[String]] = Map(),
  categories:List[String] = List(),
  links:Links = Links(List()),
  image:Image = Image("","",""),
//  market_data:MarketData,
  last_updated:Option[String] = None,

  asset_platform_id:Option[String] = None,
  detail_platforms:Map[String,DetailPlatform] = Map()

) extends Ingestable {
  
  def toToken:Token = {
    Token(
      id,symbol,name,
      contract_address,
      categories,
      icon = Option(image.large),
      src = Some(DataSource.id("coingecko")),
      dcml = detail_platforms.get(asset_platform_id.getOrElse("ethereum")).flatMap(_.decimal_place),
      chain = detail_platforms.map{ case(nid,dp) => TokenBlockchain(nid,dp.contract_address)}.toSeq
    )
  }
}

