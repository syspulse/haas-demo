package io.syspulse.haas.intercept

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import java.util.concurrent.TimeUnit
import scala.concurrent.Awaitable
import scala.concurrent.{Await, ExecutionContext, Future}
import akka.actor.typed.scaladsl.Behaviors

import com.typesafe.scalalogging.Logger
import io.jvm.uuid._

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.skel.crypto.eth.abi._

import io.syspulse.haas.intercept.flow._
import io.syspulse.haas.intercept.flow.eth._
import io.syspulse.haas.intercept._
import io.syspulse.haas.intercept.store._
import io.syspulse.haas.intercept.server._

import io.syspulse.haas.intercept.flow.eth.InterceptorTokenTransfer
case class Config(  
  host:String="0.0.0.0",
  port:Int=8080,
  uri:String = "/api/v1/intercept",
  
  feed:String = "",
  output:String = "",
  
  feedTx:String = "null://",
  feedBlock:String = "null://",
  feedToken:String = "null://",
  feedEvent:String = "null://",
  feedFunc:String = "null://",

  alarms:Seq[String] = Seq("stdout://"),
  alarmsThrottle:Long = 10000L,

  datastore:String = "dir://store/intercept",
  scriptStore:String = "dir://store/script",
  abiStore: String = "dir://store/abi",
  eventStore:String = "dir://store/event",
  funcStore:String = "dir://store/func",

  source:String="",
  
  size:Long = Long.MaxValue,
  limit:Long = Long.MaxValue,

  freq: Long = 0L,
  delimiter:String = "\n",
  buffer:Int = 1024*1024,
  throttle:Long = 0L,
  
  entity:String = "tx",
  
  expr:String = "",
  
  filter:Seq[String] = Seq(),

  cmd:String = "intercept",
  params: Seq[String] = Seq(),
  sinks:Seq[String] = Seq()
)

object App extends skel.Server {
  
  def main(args:Array[String]):Unit = {
    Console.err.println(s"args: '${args.mkString(",")}': ${args.toList}")

    val d = Config()

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"haas-intercept","",
        ArgString('h', "http.host",s"listen host (def: ${d.host})"),
        ArgInt('p', "http.port",s"listern port (def: ${d.port})"),
        ArgString('u', "http.uri",s"api uri (def: ${d.uri})"),
        
        ArgString('f', "feed",s"Input Feed (def: ${d.feed})"),
        ArgString('o', "output",s"Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log) def=${d.output}"),
        ArgString('e', "entity",s"Ingest entity: (tx,block,block-tx,token,log) def=${d.entity}"),

        ArgString('_', "feed.tx",s"Tx Feed (def: ${d.feedTx})"),
        ArgString('_', "feed.block",s"Block Feed (def: ${d.feedBlock})"),
        ArgString('_', "feed.token",s"Token Feed (def: ${d.feedToken})"),
        ArgString('_', "feed.event",s"EventLog Feed (def: ${d.feedEvent})"),
        ArgString('_', "feed.func",s"Function Tx Feed (def: ${d.feedFunc})"),

        ArgLong('_', "limit",s"Limit for entities to output (def=${d.limit})"),
        ArgLong('_', "size",s"Size limit for output (def=${d.size})"),

        ArgLong('_', "freq",s"Frequency (def=${d.freq}"),
        ArgString('_', "delimiter","""Delimiter characteds (def: '\n'). Usage example: --delimiter=`echo -e $"\r"` """),
        ArgInt('_', "buffer",s"Frame buffer (Akka Framing) (def: ${d.buffer})"),
        ArgLong('_', "throttle",s"Throttle messages in msec (def: ${d.throttle})"),

        ArgString('t', "filter",s"Filter (def='${d.filter}')"),
        
        ArgString('d', "datastore",s"datastore for intercetpions (def: ${d.datastore})"),
        ArgString('s', "store.script",s"datastore for Scripts to execute on TX (def=${d.scriptStore})"),
        ArgString('_', "store.abi",s"ABI definitions store (def: ${d.abiStore})"),
        ArgString('_', "store.event",s"Event Signatures store (def: ${d.eventStore})"),
        ArgString('_', "store.func",s"Function signatures store (def: ${d.funcStore})"),

        ArgString('a', "alarms",s"Alarms to generate on script triggers (ske-notify format, ex: email://user@mail.com ) (def=${d.alarms})"),
        ArgLong('_', "alarms.throttle",s"Throttle alarms (def=${d.alarmsThrottle})"),
        
        ArgCmd("server",s"Server"),
        ArgCmd("intercept",s"Intercept pipeline"),
        
        ArgParam("<params>","")
      ).withExit(1)
    ))

    implicit val config = Config(
      host = c.getString("http.host").getOrElse(d.host),
      port = c.getInt("http.port").getOrElse(d.port),
      uri = c.getString("http.uri").getOrElse(d.uri),
      
      feed = c.getString("feed").getOrElse(d.feed),
      output = c.getString("output").getOrElse(d.output),
      entity = c.getString("entity").getOrElse(d.entity),

      feedTx = c.getString("feed.tx").getOrElse(d.feedTx),
      feedBlock = c.getString("feed.block").getOrElse(d.feedBlock),
      feedToken = c.getString("feed.token").getOrElse(d.feedToken),
      feedEvent = c.getString("feed.event").getOrElse(d.feedEvent),
      feedFunc = c.getString("feed.func").getOrElse(d.feedFunc),

      limit = c.getLong("limit").getOrElse(d.limit),
      size = c.getLong("size").getOrElse(d.size),

      freq = c.getLong("freq").getOrElse(d.freq),
      delimiter = c.getString("delimiter").getOrElse(d.delimiter),
      buffer = c.getInt("buffer").getOrElse(d.buffer),
      throttle = c.getLong("throttle").getOrElse(d.throttle),     

      filter = c.getListString("filter",d.filter),
            
      alarms = c.getListString("alarms",d.alarms),
      alarmsThrottle = c.getLong("alarms.throttle").getOrElse(d.alarmsThrottle),

      datastore = c.getString("datastore").getOrElse(d.datastore),
      scriptStore = c.getString("store.script").getOrElse(d.scriptStore),
      abiStore = c.getString("store.abi").getOrElse(d.abiStore),
      eventStore = c.getString("store.event").getOrElse(d.eventStore),
      funcStore = c.getString("store.func").getOrElse(d.funcStore),
      
      cmd = c.getCmd().getOrElse(d.cmd),
      
      params = c.getParams(),
    )

    Console.err.println(s"Config: ${config}")

    val datastoreScripts = config.scriptStore.split("://").toList match {
      // case "mysql" | "db" => new TokenStoreDB(c,"mysql")
      // case "postgres" => new TokenStoreDB(c,"postgres")
      case "mem" :: _ => new ScriptStoreMem
      case "dir" :: dir :: Nil => new ScriptStoreDir(dir)
      case "dir" :: Nil => new ScriptStoreDir()
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.scriptStore}'")
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

    val eventStore = config.eventStore.split("://").toList match {
      case "dir" :: dir :: _ => new EventSignatureStoreDir(dir)
      case dir :: Nil => new EventSignatureStoreDir(dir)
      case "mem" :: _ => new SignatureStoreMem[EventSignature]()
      case _ => new SignatureStoreMem[EventSignature]()
    }

    val funcStore = config.funcStore.split("://").toList match {
      case "dir" :: dir :: _ => new FuncSignatureStoreDir(dir)
      case dir :: Nil => new FuncSignatureStoreDir(dir)
      case "mem" :: _ => new SignatureStoreMem[FuncSignature]()
      case _ => new SignatureStoreMem[FuncSignature]()
    }

    val abiStore = config.abiStore.split("://").toList match {
      case "dir" :: dir :: _ => new AbiStoreDir(dir,funcStore,eventStore) 
      case dir :: Nil => new AbiStoreDir(dir,funcStore,eventStore) 
      case _ => new AbiStoreDir(config.abiStore,funcStore,eventStore) 
    }

    abiStore.load()

    
    val (r,pp) = config.cmd match {
      case "server" => {
        val ixTx = new InterceptorTx(datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
        val ppTx = new PipelineEthInterceptTx(if(config.feedTx.nonEmpty) config.feedTx else config.feed, config.output,ixTx)(config)

        val ixBlock = new InterceptorBlock(datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
        val ppBlock = new PipelineEthInterceptBlock(if(config.feedBlock.nonEmpty) config.feedBlock else config.feed, config.output,ixBlock)(config)

        val ixToken = new InterceptorTokenTransfer(datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
        val ppToken = new PipelineEthInterceptTokenTransfer(if(config.feedToken.nonEmpty) config.feedToken else config.feed, config.output,ixToken)(config)

        val ixEvent = new InterceptorEvent(abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
        val ppEvent = new PipelineEthInterceptEvent(if(config.feedEvent.nonEmpty) config.feedEvent else config.feed, config.output,ixEvent)(config)

        val ixFunc = new InterceptorFunc(abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
        val ppFunc = new PipelineEthInterceptFunc(if(config.feedFunc.nonEmpty) config.feedFunc else config.feed, config.output,ixFunc)(config)

        run( config.host, config.port, config.uri, c,
          Seq(
            (Behaviors.ignore,"",(actor,actorSystem) => new AlarmRoutes("ws")(actorSystem) ),
            (InterceptionRegistry(datastoreInterceptions,
                                  datastoreScripts,
                                  abiStore,
                                  Map(
                                    ixTx.entity() -> ixTx,
                                    ixBlock.entity() -> ixBlock,
                                    ixToken.entity() -> ixToken,
                                    ixEvent.entity() -> ixEvent,
                                    ixFunc.entity() -> ixFunc,
                                  )),
              "InterceptionRegistry",(r, ac) => new InterceptionRoutes(r)(ac) )
          )
        )
        // start pipeline
        val r1 = ppTx.run()
        val r2 = ppBlock.run()
        val r3 = ppToken.run()
        val r4 = ppEvent.run()
        val r5 = ppFunc.run()
        
        (r1,Some(ppTx))
      }

      case "intercept" => {        
        def buildInterceptions(alarms:Seq[String]):Seq[Interception] = {
          alarms.map(a => { 
              val ix:Interception = a.split("=").toList match {
                case sid :: typ :: ua :: Nil => 
                  Interception(UUID.random, "Ix-1", sid, ua.split(";").toList, entity = typ)
                case sid :: ua :: Nil => 
                  Interception(UUID.random, "Ix-1", sid, ua.split(";").toList)
                case ua :: Nil => 
                  Interception(UUID.random, "Ix-2", "script-1.js", ua.split(";").toList)
                case _ => 
                  Interception(UUID.random, "Ix-3", "script-1.js", List("stdout://"))
              }
              ix
            }
          )
        }

        val pp = config.entity match {
          case "block" =>
            new PipelineEthInterceptBlock(config.feed,config.output, 
                new InterceptorBlock(datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))
          case "tx" =>
            new PipelineEthInterceptTx(config.feed,config.output, 
                new InterceptorTx(datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))
          case "token" =>
            new PipelineEthInterceptTokenTransfer(config.feed,config.output, 
                new InterceptorTokenTransfer(datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)                          
          case "erc20" =>
            // not useful Interceptor, only for Testing
            new PipelineEthInterceptTx(config.feed,config.output, 
                new InterceptorERC20(abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)
          case "event" | "log" =>
            new PipelineEthInterceptEvent(config.feed,config.output, 
                new InterceptorEvent(abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)
          case "func" =>
            new PipelineEthInterceptFunc(config.feed,config.output, 
                new InterceptorFunc(abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)
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