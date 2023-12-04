package io.syspulse.haas.ingest.icp

/* 
icp://host:port/api
*/

case class IcpURI(rpcUri:String,apiSuffix:String="",apiToken:String="") {
  val PREFIX = "icp://"

  val DEFAULT_HOST = "rosetta-api.internetcomputer.org"
  val DEFAULT_BLOCKCHAIN = "Internet Computer"
  val DEFAULT_NETWORK = "00000000000000020101"
  def rpcUrl(apiToken:String = "") = s""
  
  private var rBlockchain = DEFAULT_BLOCKCHAIN
  private var rNetwork = DEFAULT_NETWORK
  private var rUri = ""

  def blockchain:String = rBlockchain
  def network:String = rNetwork
  def uri:String = rUri

  def getBlockchain(blockchain:String):(String,String) = blockchain.split("\\:").toList match {
    case blockchain :: network :: _ => (blockchain,network)
    case "" :: Nil => (DEFAULT_BLOCKCHAIN,DEFAULT_NETWORK)
    case network :: Nil => (DEFAULT_BLOCKCHAIN,network)
    case Nil => (DEFAULT_BLOCKCHAIN,DEFAULT_NETWORK)
  }

  def parse(rpcUri:String):(String,String,String) = {
    val prefix = "https://"

    rpcUri.trim.stripPrefix(PREFIX).split("/|@").toList match {
      case blockchain :: Nil if(rpcUri.contains("@")) => 
        val b = getBlockchain(blockchain)
        (b._1, b._2, prefix + DEFAULT_HOST + "/" + rpcUrl(apiToken) + apiSuffix)
      
      case blockchain :: path if(rpcUri.contains("@")) => 
        val b = getBlockchain(blockchain)
        (b._1, b._2, prefix + path.mkString("/"))
                  
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
      rUri = u.replaceAll("\\/+$","")
  }
}