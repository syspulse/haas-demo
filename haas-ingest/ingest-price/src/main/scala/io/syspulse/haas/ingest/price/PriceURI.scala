package io.syspulse.haas.ingest.price

/* 
price://host:port/api
*/

case class PriceURI(uri:String,apiSuffix:String="") {
  
  def build(prefix:String,host:String,url:String,apiSuffix:String) = {
    prefix + host + url + apiSuffix
  }

  def parse():Option[String] = {
    val prefix = "https://"

    uri.trim.split("://").toList match {
      case "http" :: host :: Nil => Some(build("http://",host,"/data/",apiSuffix))
      case "cryptocomp" :: host :: Nil => Some(build("http://",host,"/api/",""))
      case "cryptocomp" :: Nil => Some(build("https://","min-api.cryptocompare.com","/data/",apiSuffix))
      
      case "coingecko" :: Nil => Some(build("https://","api.coingecko.com/api/v3","/simple/price/",apiSuffix))

      case "chainlink" :: Nil => Some(build("http://","geth.hacken.cloud:8545","",apiSuffix))
            
      case _ => None
    }
  }
}