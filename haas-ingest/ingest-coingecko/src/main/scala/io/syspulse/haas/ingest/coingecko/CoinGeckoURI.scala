package io.syspulse.haas.ingest.coingecko

/* 
coingecko://host:port/api
*/

case class CoingeckoURI(cgUri:String,apiSuffix:String="") {
  val PREFIX = "coingecko://"

  val coingeckoHost = "api.coingecko.com"
  val coingeckoUrl = "api/v3"

  def uri:String = {
    val prefix = "https://"

    cgUri.trim.stripPrefix(PREFIX).split("[/]").toList match {
      case host :: path :: _ => prefix + host + path
      case "" :: Nil => prefix + coingeckoHost + "/" + coingeckoUrl + apiSuffix
      case host :: Nil => prefix + host + "/" + coingeckoUrl + apiSuffix
      case Nil => prefix + coingeckoHost + "/" + coingeckoUrl + apiSuffix
    }
  }
}