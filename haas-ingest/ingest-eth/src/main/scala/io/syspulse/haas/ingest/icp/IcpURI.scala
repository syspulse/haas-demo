package io.syspulse.haas.ingest.icp

/* 
icp://host:port/api
*/

case class IcpURI(rpcUri:String,apiSuffix:String="",apiToken:String="") {
  val PREFIX = "icp://"

  val DEFAULT_HOST = "rosetta-api.internetcomputer.org"
  def rpcUrl(apiToken:String = "") = s""
  
  private var rBlockchain = "Internet Computer"
  private var rNetwork = "00000000000000020101"
  private var rUri = ""

  def blockchain:String = rBlockchain
  def network:String = rNetwork
  def uri:String = rUri

  def getBlockchain(blockchain:String):(String,String) = blockchain.split("\\:").toList match {
    case blockchain :: network :: _ => (blockchain,network)
    case "" :: Nil => ("Internet Computer","00000000000000020101")
    case network :: Nil => ("Internet Computer",network)
    case Nil => ("Internet Computer","00000000000000020101")
  }

  def parse(rpcUri:String):(String,String,String) = {
    val prefix = "https://"

    rpcUri.trim.stripPrefix(PREFIX).split("/|@").toList match {
      case blockchain :: path if(rpcUri.contains("@")) => 
        val b = getBlockchain(blockchain)
        (b._1, b._2, prefix + path)
      
      case "" :: Nil if(rpcUri.contains("@")) => 
        val b = getBlockchain(blockchain)
        (b._1, b._2, prefix + DEFAULT_HOST + "/" + rpcUrl(apiToken) + apiSuffix)

      case blockchain :: Nil if(rpcUri.contains("@")) => 
        val b = getBlockchain(blockchain)
        (b._1, b._2, prefix + DEFAULT_HOST + "/" + rpcUrl(apiToken) + apiSuffix)
      
      case host :: path :: _ => 
        val b = getBlockchain("")
        (b._1, b._2, prefix + host + path)
      case "" :: Nil => 
        val b = getBlockchain("")
        (b._1, b._2, prefix + DEFAULT_HOST + "/" + rpcUrl(apiToken) + apiSuffix)
      case host :: Nil => 
        val b = getBlockchain("")
        (b._1, b._2, prefix + DEFAULT_HOST + "/" + rpcUrl(apiToken) + apiSuffix)
      case Nil => 
        val b = getBlockchain("")
        (b._1, b._2, prefix + DEFAULT_HOST + "/" + rpcUrl(apiToken) + apiSuffix)
    }
  }

  parse(rpcUri) match {
    case (b,n,u) => 
      rBlockchain = b
      rNetwork = n
      rUri = u
  }
}