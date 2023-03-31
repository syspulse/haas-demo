package io.syspulse.haas.ingest.coingecko

/* 
coingecko://{API_KEY}@
coingecko://{API_KEY}@host:port/api
coingecko://host/api
*/

case class CoinGeckoURI(cgUri:String,apiSuffix:String="") {
  val PREFIX = "coingecko://"

  val coingeckoHost = "api.coingecko.com"
  val coingeckoHostPro = "pro-api.coingecko.com"
  val coingeckoApiVer = "api/v3"

  val apiKeyParam="x_cg_pro_api_key"

  val p0 = """coingecko://""".r
  val p1 = """coingecko://\{(\S+)\}@""".r
  val p2 = """coingecko://(\S+)@""".r
  val p3 = """coingecko://(\S+)""".r

  def resolveApiKey(apiKey:String) = {
    if(apiKey.startsWith("{")) 
      sys.env.get(apiKey.stripPrefix("{").stripSuffix("}")).getOrElse("") 
    else
      apiKey
  }

  def uri:String = {
    val prefix = "https://"

    cgUri.trim match {
      case p0() => 
        prefix + coingeckoHost + "/" + coingeckoApiVer + apiSuffix

      case p1(apiKey) =>
        val cgApiKey = sys.env.get(apiKey).getOrElse("")
        prefix + coingeckoHostPro + "/" + coingeckoApiVer + apiSuffix + {if(apiSuffix.isEmpty) "?" else "&" } + s"${apiKeyParam}=${cgApiKey}"

      case p2(apiKey) =>
        val cgApiKey = apiKey
        prefix + coingeckoHostPro + "/" + coingeckoApiVer + apiSuffix + {if(apiSuffix.isEmpty) "?" else "&" } + s"${apiKeyParam}=${cgApiKey}"

      case p3(host) => 
        prefix + host + "/" + coingeckoApiVer + apiSuffix
    }
  }

}