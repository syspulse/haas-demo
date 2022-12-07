package io.syspulse.haas.ingest.eth

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import java.util.concurrent.TimeUnit
import scala.concurrent.Awaitable
import scala.concurrent.{Await, ExecutionContext, Future}

import com.typesafe.scalalogging.Logger
import io.jvm.uuid._

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.skel.ingest.flow.Pipeline
import io.syspulse.haas.ingest.eth.flow._
import io.syspulse.haas.ingest.eth.intercept._
import io.syspulse.haas.ingest.eth.store._
import io.syspulse.haas.ingest.eth.server._
import akka.actor.typed.scaladsl.Behaviors

object App extends skel.Server {
  
  def main(args:Array[String]):Unit = {
    Console.err.println(s"args: '${args.mkString(",")}'")

    val d = Config()

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"ingest-eth","",
        ArgString('h', "http.host",s"listen host (def: ${d.host})"),
        ArgInt('p', "http.port",s"listern port (def: ${d.port})"),
        ArgString('u', "http.uri",s"api uri (def: ${d.uri})"),
        
        ArgString('f', "feed",s"Input Feed (def: ${d.feed})"),
        ArgString('o', "output",s"Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log) def=${d.output}"),
        ArgString('e', "entity",s"Ingest entity: (tx,block,block-tx,token,log) def=${d.entity}"),

        ArgLong('_', "limit",s"Limit for entities to output (def=${d.limit})"),
        ArgLong('_', "size",s"Size limit for output (def=${d.size})"),

        ArgLong('_', "freq",s"Frequency (def=${d.freq}"),
        ArgString('_', "delimiter","""Delimiter characteds (def: '\n'). Usage example: --delimiter=`echo -e $"\r"` """),
        ArgInt('_', "buffer",s"Frame buffer (Akka Framing) (def: ${d.buffer})"),
        ArgLong('_', "throttle",s"Throttle messages in msec (def: ${d.throttle})"),

        ArgString('t', "filter",s"Filter (def='${d.filter}')"),
        
        ArgString('d', "datastore",s"datastore for intercetpions (def: ${d.datastore})"),
        ArgString('s', "scripts",s"datastore for Scripts to execute on TX (def=${d.scripts})"),
        ArgString('_', "abi",s"directory with ABI jsons (format: NAME-0xaddress.json) (def=${d.abi}"),


        ArgString('a', "alarms",s"Alarms to generate on script triggers (ske-notify format, ex: email://user@mail.com ) (def=${d.alarms})"),
        ArgLong('_', "alarms.throttle",s"Throttle alarms (def=${d.alarmsThrottle})"),
        
        ArgCmd("server",s"Server"),
        ArgCmd("ingest",s"Ingest pipeline (requires -e <entity>)"),
        ArgCmd("intercept",s"Intercept pipeline (-s script)"),
        
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      host = c.getString("http.host").getOrElse(d.host),
      port = c.getInt("http.port").getOrElse(d.port),
      uri = c.getString("http.uri").getOrElse(d.uri),
      
      feed = c.getString("feed").getOrElse(d.feed),
      output = c.getString("output").getOrElse(d.output),
      entity = c.getString("entity").getOrElse(d.entity),

      limit = c.getLong("limit").getOrElse(d.limit),
      size = c.getLong("size").getOrElse(d.size),

      freq = c.getLong("freq").getOrElse(d.freq),
      delimiter = c.getString("delimiter").getOrElse(d.delimiter),
      buffer = c.getInt("buffer").getOrElse(d.buffer),
      throttle = c.getLong("throttle").getOrElse(d.throttle),     

      filter = c.getListString("filter",d.filter),
      
      datastore = c.getString("datastore").getOrElse(d.datastore),
      scripts = c.getString("scripts").getOrElse(d.scripts),
      alarms = c.getListString("alarms",d.alarms),
      alarmsThrottle = c.getLong("alarms.throttle").getOrElse(d.alarmsThrottle),

      abi = c.getString("abi").getOrElse(d.abi),
      
      cmd = c.getCmd().getOrElse(d.cmd),
      
      params = c.getParams(),
    )

    Console.err.println(s"Config: ${config}")

    val datastoreScripts = config.scripts.split("://").toList match {
      // case "mysql" | "db" => new TokenStoreDB(c,"mysql")
      // case "postgres" => new TokenStoreDB(c,"postgres")
      case "mem" :: _ => new ScriptStoreMem
      case "dir" :: dir :: Nil => new ScriptStoreDir(dir)
      case "dir" :: Nil => new ScriptStoreDir()
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.scripts}'")
        sys.exit(1)
      }
    }

    val datastoreInterceptions = config.datastore.split("://").toList match {
      // case "mysql" | "db" => new TokenStoreDB(c,"mysql")
      // case "postgres" => new TokenStoreDB(c,"postgres")
      case "mem" :: _ => new InterceptionStoreMem
      case "dir" :: dir :: Nil => new InterceptionStoreDir(dir)
      case "dir" :: Nil => new InterceptionStoreDir()
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}'")
        sys.exit(1)
      }
    }

    
    val (r,pp) = config.cmd match {
      case "server" =>
        val ix = new InterceptorTx(datastoreInterceptions,datastoreScripts,config.alarmsThrottle) 
        val pp = new PipelineEthInterceptTx(config.feed,config.output,ix)(config)

        run( config.host, config.port, config.uri, c,
          Seq(
            (Behaviors.ignore,"",(actor,actorSystem) => new AlarmRoutes("ws")(actorSystem) ),
            (InterceptionRegistry(datastoreInterceptions,datastoreScripts,ix),
              "InterceptionRegistry",(r, ac) => new InterceptionRoutes(r)(ac) )
          )
        )
        // start pipeline
        val r = pp.run()
        
        (r,Some(pp))

      case "ingest" => {
        val pp = config.entity match {
          case "tx" =>
            new PipelineEthTx(config.feed,config.output)(config)
                    
          case "block" =>
            new PipelineEthBlock(config.feed,config.output)(config)            
          
          case "block-tx" =>
            new PipelineEthBlockTx(config.feed,config.output)(config)

          case "token" =>
            new PipelineEthTokenTransfer(config.feed,config.output)(config)

          case "log" =>
            new PipelineEthLog(config.feed,config.output)(config)

          case _ =>  Console.err.println(s"Uknown entity: '${config.entity}'"); sys.exit(1)
        } 

        (pp.run(),Some(pp))
      }

      case "intercept" => {
        def buildInterceptions(alarms:Seq[String]):Seq[Interception] = {
          alarms.map(a => { 
              val ix:Interception = a.split("=").toList match {
                case sid :: ua :: Nil => 
                  Interception(UUID.random, "Ix-1", sid, ua.split(";").toList, uid = None)
                case ua :: Nil => 
                  Interception(UUID.random, "Ix-2", "script-1.js", ua.split(";").toList, uid = None)
                case _ => 
                  Interception(UUID.random, "Ix-3", "script-1.js", List("stdout://"), uid = None)
              }
              ix
            }
          )
        }

        val pp = config.entity match {
          case "tx" =>
            new PipelineEthInterceptTx(config.feed,config.output, 
                new InterceptorTx(datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)
          case "token" =>
            new PipelineEthInterceptTokenTransfer(config.feed,config.output, 
                new InterceptorTokenTransfer(datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)
          case "erc20" =>
            new PipelineEthInterceptTx(config.feed,config.output, 
                new InterceptorERC20(datastoreInterceptions,datastoreScripts,config.alarmsThrottle,config.abi,buildInterceptions(config.alarms)))(config)
        }

        (pp.run(),Some(pp))
      }
    }
    
    Console.err.println(s"r=${r}")
    r match {
      case a:Awaitable[_] => {
        val rr = Await.result(a,FiniteDuration(300,TimeUnit.MINUTES))
        Console.err.println(s"rr: ${rr}")
      }
      case akka.NotUsed => 
    }

    Console.err.println(s"Result: ${pp.map(_.countObj)}")
    sys.exit(0)
  }
}