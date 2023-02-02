package io.syspulse.haas.ingest.price

/* 
price://host:port/api
*/

case class PriceURI(priceUri:String,apiSuffix:String="") {
  
  def build(prefix:String,host:String,url:String,apiSuffix:String) = {
    prefix + host + url + apiSuffix
  }

  def parse():Option[String] = {
    val prefix = "https://"

    priceUri.trim.split("://").toList match {
      case "http" :: host :: Nil => Some(build("http://",host,"/data/",apiSuffix))
      case "cryptocomp" :: host :: Nil => Some(build("http://",host,"/api/",""))
      case "cryptocomp" :: Nil => Some(build("https://","min-api.cryptocompare.com","/data/",apiSuffix))
      case _ => None
    }
  }
}