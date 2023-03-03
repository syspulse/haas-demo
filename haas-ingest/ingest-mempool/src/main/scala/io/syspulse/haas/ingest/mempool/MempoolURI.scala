package io.syspulse.haas.ingest.mempool
/* 
geth://host:port
*/

case class MempoolURI(uri:String,apiSuffix:String="") {
  
  def build(prefix:String,host:String,url:String,apiSuffix:String) = {
    prefix + host + url + apiSuffix
  }

  def parse():Option[String] = {
    val prefix = "https://"

    uri.trim.split("://").toList match {
      case "http" :: host :: Nil => Some(build("http://",host,"",apiSuffix))
      case "geth" :: host :: Nil => Some(build("http://",host,"",""))
      case _ => None
    }
  }
}