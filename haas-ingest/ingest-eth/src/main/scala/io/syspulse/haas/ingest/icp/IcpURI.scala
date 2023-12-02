package io.syspulse.haas.ingest.icp

/* 
icp://host:port/api
*/

case class IcpURI(rpcUri:String,apiSuffix:String="",apiToken:String="") {
  val PREFIX = "icp://"

  val rpcHost = "rosetta-api.internetcomputer.org"
  def rpcUrl(apiToken:String = "") = s""

  def uri:String = {
    val prefix = "https://"

    rpcUri.trim.stripPrefix(PREFIX).split("[/]").toList match {
      case host :: path :: _ => prefix + host + path
      case "" :: Nil => prefix + rpcHost + "/" + rpcUrl(apiToken) + apiSuffix
      case host :: Nil => prefix + host + "/" + rpcUrl(apiToken) + apiSuffix
      case Nil => prefix + rpcHost + "/" + rpcUrl(apiToken) + apiSuffix
    }
  }
}