package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import scala.concurrent.{Await, ExecutionContext, Future}

import io.syspulse.haas.ingest.store._

case class Config(  
  cgUri:String = "",  
  limit:Long = 0L,
  freq: Long = 0L,
  logFile:String = "",   
  elasticUri:String = "",
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
        ArgString('o', "elastic.uri","Elastic cluster uri"),
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
      elasticUri = c.getString("elastic.uri").getOrElse("http://localhost:5302"),
      limit = c.getLong("limit").getOrElse(0),
      freq = c.getLong("freq").getOrElse(0),

      tokens = c.getString("tokens").getOrElse("").split(",").map(_.trim).filter(!_.isEmpty()),
      
      datastore = c.getString("datastore").getOrElse("stdout"),
      
      cmd = c.getCmd().getOrElse("coins"),
      
      params = c.getParams(),
    )

    println(s"Config: ${config}")

    val store = config.datastore match {
      // case "elastic" => new CgStoreElastic()
      case "stdout" => new StoreStdout[Any]
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}'")
        System.exit(1)
        new StoreStdout[Any]
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