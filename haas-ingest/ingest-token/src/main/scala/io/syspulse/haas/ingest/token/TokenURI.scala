package io.syspulse.haas.ingest.token

import io.syspulse.haas.ingest.coingecko.CoingeckoURI

/* 
token://host:port/api
*/

case class TokenURI(uri:String,apiSuffix:String="") {
  
  def build(prefix:String,host:String,url:String,apiSuffix:String) = {
    prefix + host + url + apiSuffix
  }

  def parse():Option[String] = {
    val prefix = "https://"

    uri.trim.split("://").toList match {
      case "http" :: host :: Nil => Some(build("http://",host,"/data/",apiSuffix))
      
      // case "coingecko" :: Nil => Some(build("https://","api.coingecko.com/api/v3","/simple/price/",apiSuffix))
      case "coingecko" :: _ => Some(CoingeckoURI(uri,apiSuffix).uri)

      case _ => None
    }
  }
}