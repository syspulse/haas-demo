package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import scala.concurrent.{Await, ExecutionContext, Future}

import io.syspulse.haas.token.store.TokenStoreElastic
import io.syspulse.haas.token.store.TokenStoreMem
import io.syspulse.skel.token.store.TokenStoreStdout

case class Config(  
  cgUri:String = "",  
  
  feed:String = "",
  
  limit:Long = 0L,
  freq: Long = 0L,
  output:String = "",   
  
  elasticUri:String = "",
  elasticUser:String = "",
  elasticPass:String = "",
  elasticIndex:String = "",
  expr:String = "",
  
  datastore:String = "",

  tokens:Seq[String] = Seq(),

  cmd:String = "",
  params: Seq[String] = Seq(),
  sinks:Seq[String] = Seq()
)

object App {
  
  def main(args:Array[String]):Unit = {
    println(s"args: '${args.mkString(",")}'")

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"ingest-gecko","",
        ArgString('i', "cg.uri","Coingecko uri"),
                
        ArgString('f', "feed","Input Feed (def: )"),
        ArgString('o', "output","Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log)"),

        ArgString('_', "elastic.uri","Elastic uri (def: http://localhost:9200)"),
        ArgString('_', "elastic.user","Elastic user (def: )"),
        ArgString('_', "elastic.pass","Elastic pass (def: )"),
        ArgString('_', "elastic.index","Elastic Index (def: video)"),


        ArgLong('l', "limit","Limit"),
        ArgLong('f', "freq","Frequency"),

        ArgString('t', "tokens","Tokens filter (ex: 'UNI,ETH')"),
        
        ArgString('d', "datastore","datastore [elastic,stdout,file] (def: stdout)"),
        
        ArgCmd("coins","All coins list"),
        ArgCmd("coin","Coin Detailed info"),
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      cgUri = c.getString("cg.uri").getOrElse("http://localhost:8100"),
      
      elasticUri = c.getString("elastic.uri").getOrElse("http://localhost:9200"),
      elasticIndex = c.getString("elastic.index").getOrElse("token"),
      elasticUser = c.getString("elastic.user").getOrElse(""),
      elasticPass = c.getString("elastic.pass").getOrElse(""),
      
      feed = c.getString("feed").getOrElse(""),

      limit = c.getLong("limit").getOrElse(0),
      freq = c.getLong("freq").getOrElse(0),

      tokens = c.getString("tokens").getOrElse("").split(",").map(_.trim).filter(!_.isEmpty()),
      
      datastore = c.getString("datastore").getOrElse("stdout"),
      
      cmd = c.getCmd().getOrElse("coins"),
      
      params = c.getParams(),
    )

    println(s"Config: ${config}")

    val store = config.datastore match {      
      case "elastic" => new TokenStoreElastic().connect(config.elasticUri,config.elasticIndex)
      case "mem" => new TokenStoreMem()
      case "stdout" => new TokenStoreStdout()
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}'")
        sys.exit(1)
      }
    }

    config.cmd match {
        
      case "coins" => 
        new CoingeckoIngestCoins(config,c).run()
      case "coin" => 
        new CoingeckoIngestCoinInfo(config,c).run()
      
    }
  }
}