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

import io.syspulse.haas.ingest.gecko.flow.PipelineCoins
import java.util.concurrent.TimeUnit
import scala.concurrent.Awaitable

case class Config(  
  cgUri:String = "",  
  
  feed:String = "",
  output:String = "",
  
  limit:Long = 0L,
  freq: Long = 0L,
  delimiter:String = "",
  buffer:Int = 0,
  throttle:Long = 0L,
  
  entity:String = "",
  
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
    Console.err.println(s"args: '${args.mkString(",")}'")

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"ingest-gecko","",
        ArgString('i', "cg.uri","Coingecko uri"),
                
        ArgString('f', "feed","Input Feed (def: )"),
        ArgString('o', "output","Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log)"),
        ArgString('e', "entity","Ingest entity: (coin,coins)"),

        ArgString('_', "elastic.uri","Elastic uri (def: http://localhost:9200)"),
        ArgString('_', "elastic.user","Elastic user (def: )"),
        ArgString('_', "elastic.pass","Elastic pass (def: )"),
        ArgString('_', "elastic.index","Elastic Index (def: video)"),

        ArgLong('_', "limit","Limit"),
        ArgLong('_', "freq","Frequency"),
        ArgString('_', "delimiter","""Delimiter characteds (def: ''). Usage example: --delimiter=`echo -e $"\r"` """),
        ArgInt('_', "buffer","Frame buffer (Akka Framing) (def: 1M)"),
        ArgLong('_', "throttle","Throttle messages in msec (def: 0)"),

        ArgString('t', "tokens","Tokens filter (ex: 'UNI,ETH')"),
        
        ArgString('d', "datastore","datastore [elastic,stdout,file] (def: stdout)"),
        
        ArgCmd("ingest","Ingest"),
        ArgCmd("ingest-old","Ingest old"),
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
      output = c.getString("output").getOrElse(""),
      entity = c.getString("entity").getOrElse("coins"),

      limit = c.getLong("limit").getOrElse(0),
      freq = c.getLong("freq").getOrElse(0),
      delimiter = c.getString("delimiter").getOrElse(""),
      buffer = c.getInt("buffer").getOrElse(1024*1024),
      throttle = c.getLong("throttle").getOrElse(0L),     

      tokens = c.getString("tokens").getOrElse("").split(",").map(_.trim).filter(!_.isEmpty()),
      
      datastore = c.getString("datastore").getOrElse("stdout"),
      
      cmd = c.getCmd().getOrElse("ingest"),
      
      params = c.getParams(),
    )

    Console.err.println(s"Config: ${config}")

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
      case "ingest" => {
        val pp = config.entity match {
          case "coins" =>
            new PipelineCoins(config.feed,config.output)(config)            
          case _ =>  Console.err.println(s"Uknown entity: '${config.entity}'"); sys.exit(1)
        } 

        val r = pp.run
        r match {
          case a:Awaitable[_] => Await.ready(a,FiniteDuration(30,TimeUnit.MINUTES))
          case akka.NotUsed => 
        }

        Console.err.println(s"Tokens: ${pp.countObj}")
        sys.exit(0)
      }

      // case "ingest-old" => {
      //   config.entity match {
      //     case "coins" => 
      //       new IngestCoins(config,c).run()
      //     case "coin" => 
      //       new IngestCoinInfo(config,c).run()
      //     case _ =>  Console.err.println(s"Uknown entity: '${config.entity}'"); sys.exit(1)
      //   }
      // }
    }
  }
}