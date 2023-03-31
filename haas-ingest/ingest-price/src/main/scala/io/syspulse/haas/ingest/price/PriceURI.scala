package io.syspulse.haas.ingest.price

import io.syspulse.haas.ingest.coingecko.CoinGeckoURI


case class PriceURI(uri:String,apiSuffix:String="") {
  
  def build(prefix:String,host:String,url:String,apiSuffix:String) = {
    prefix + host + url + apiSuffix
  }

  def parse():Option[String] = {
    val prefix = "https://"

    uri.trim.split("://").toList match {
      case "http" :: host :: Nil => 
        Some(build("http://",host,"/data/",apiSuffix))
      
      case "cryptocomp" :: host :: Nil => 
        Some(build("http://",host,"/api/",""))

      case "cryptocomp" :: _ => 
        Some(build("https://","min-api.cryptocompare.com","/data/",apiSuffix))
      
      case "coingecko" :: _ =>         
        //Some(build("https://","api.coingecko.com/api/v3","/simple/price/",apiSuffix))
        Some(CoinGeckoURI(uri,"/simple/price/"+apiSuffix).uri)

      case "chainlink" :: _ => 
        Some(build("http://","geth2.hacken.cloud:8545","",apiSuffix))
            
      case _ => None
    }
  }
}