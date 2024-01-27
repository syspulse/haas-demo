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
import io.syspulse.haas.core.Blockchain

import io.syspulse.haas.intercept.flow.etl._

case class ConfigBlockchain(
  bid:Blockchain.ID,
  feedTransaction:String = "stdin://",
  feedBlock:String = "null://",
  feedToken:String = "null://",
  feedEvent:String = "null://",
  feedLog:String = "null://",
  feedFunc:String = "null://",
  feedMempool:String = "null://",
  feedTx:String = "stdin://",
)

case class Config(  
  host:String="0.0.0.0",
  port:Int=8080,
  uri:String = "/api/v1/intercept",
  
  feed:String = "",
  output:String = "",
  
  // defaults
  feedTransaction:String = "stdin://",
  feedBlock:String = "null://",
  feedToken:String = "null://",
  feedEvent:String = "null://",
  feedLog:String = "null://",
  feedFunc:String = "null://",
  feedMempool:String = "null://",
  feedTx:String = "stdin://",

  bid:Seq[String] = Seq("ethereum"),
  blockchains:Map[Blockchain.ID,ConfigBlockchain] = Map(),

  alarms:Seq[String] = Seq("stdout://"),
  alarmsThrottle:Long = 10000L,

  datastore:String = "dir://store/intercept",
  scriptStore:String = "dir://store/script",
  abiStore: String = "dir://store/abi",
  // eventStore:String = "dir://store/event",
  // funcStore:String = "dir://store/func",

  source:String="",
  
  size:Long = Long.MaxValue,
  limit:Long = Long.MaxValue,

  freq: Long = 0L,
  delimiter:String = "\n",
  buffer:Int = 1024*1024,
  throttle:Long = 0L,
  
  entity:String = "transaction",
  history:Int = 10,
  
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
        
        ArgString('f', "input",s"Input Feed (def: ${d.feed})"),
        ArgString('o', "output",s"Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log) def=${d.output}"),
        ArgString('e', "entity",s"Ingest entity: (tx,block,block-tx,token,log) def=${d.entity}"),

        ArgString('_', "bid",s"Blockchains (def=${d.bid})"),

        ArgString('_', "feed.{bid}.transaction",s"Transaction Feed"),
        ArgString('_', "feed.{bid}.block",s"Block Feed"),
        ArgString('_', "feed.{bid}.token",s"TokenTransfe Feed"),
        ArgString('_', "feed.{bid}.event",s"EventLog Feed"),
        ArgString('_', "feed.{bid}.log",s"EventLog Feed (synonim)"),
        ArgString('_', "feed.{bid}.func",s"Function Feed"),
        ArgString('_', "feed.{bid}.mempool",s"Mempool Feed"),
        ArgString('_', "feed.{bid}.tx",s"Tx Feed"),

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
 
        ArgString('a', "alarms",s"Alert Triggers (ex: script-1.js=tx=none://). Notification is skel-notify) (def=${d.alarms})"),
        ArgLong('_', "alarms.throttle",s"Throttle alarms (def=${d.alarmsThrottle})"),

        ArgInt('_', "history",s"History limit for alarms (def=${d.history})"),
        
        ArgCmd("server",s"Server"),
        ArgCmd("intercept",s"Intercept pipeline"),
        
        ArgParam("<params>",""),
        ArgLogging()
        
      ).withExit(1)
    )).withLogging()

    implicit val config = Config(
      host = c.getString("http.host").getOrElse(d.host),
      port = c.getInt("http.port").getOrElse(d.port),
      uri = c.getString("http.uri").getOrElse(d.uri),
      
      feed = c.getString("input").getOrElse(d.feed),
      output = c.getString("output").getOrElse(d.output),
      entity = c.getString("entity").getOrElse(d.entity),

      bid = c.getListString("bid",d.bid),
      blockchains = c.getListString("bid",d.bid).map( bid => 
        bid -> ConfigBlockchain(bid,
          feedTransaction = c.getString(s"feed.${bid}.transaction").getOrElse(d.feedTransaction),
          feedBlock = c.getString(s"feed.${bid}.block").getOrElse(d.feedBlock),
          feedToken = c.getString(s"feed.${bid}.token").getOrElse(d.feedToken),
          feedEvent = c.getString(s"feed.${bid}.event").getOrElse(d.feedEvent),
          feedLog = c.getString(s"feed.${bid}.log").getOrElse(d.feedLog),
          feedFunc = c.getString(s"feed.${bid}.func").getOrElse(d.feedFunc),
          feedMempool = c.getString(s"feed.${bid}.mempool").getOrElse(d.feedMempool),
          feedTx = c.getString(s"feed.${bid}.tx").getOrElse(d.feedTx),
        )
      ).toMap,
      
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
      
      history = c.getInt("history").getOrElse(d.history),     

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

    val abiStore:AbiStore = config.abiStore.split("://").toList match {
      case "dir" :: dir :: Nil => 
        new AbiStoreDir(dir + "/contract", 
          new FuncSignatureStoreDir(dir+ "/func"),
          new EventSignatureStoreDir(dir+"/event"),
        )
      case "dir" :: Nil => 
        new AbiStoreDir("store/abi/contact", 
          new FuncSignatureStoreDir("store/abi/func"),
          new EventSignatureStoreDir("store/abi/event"),
        )
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}")
        sys.exit(1)
      }
    }

    abiStore.load()

    
    val (r,pp) = config.cmd match {
      case "server" => {

        val blockchainInterceptors = config.blockchains.values.map( b => {

          val ixTransaction = new InterceptorTransaction(b.bid,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
          val ppTransaction = new PipelineETLInterceptTransaction(
            if(!b.bid.isEmpty) config.blockchains(b.bid).feedTransaction else config.feed, config.output,ixTransaction)(config)
          
          val ixBlock = new InterceptorBlock(b.bid,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
          val ppBlock = new PipelineETLInterceptBlock(
            if(!b.bid.isEmpty) config.blockchains(b.bid).feedBlock else config.feed, config.output,ixBlock)(config)
          
          val ixToken = new InterceptorTokenTransfer(b.bid,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
          val ppToken = new PipelineETLInterceptTokenTransfer(
            if(!b.bid.isEmpty) config.blockchains(b.bid).feedToken else config.feed, config.output,ixToken)(config)
          
          val ixEvent = new InterceptorEvent(b.bid,abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
          val ppEvent = new PipelineETLInterceptEvent(
            if(!b.bid.isEmpty) {
              if(config.blockchains(b.bid).feedEvent.isEmpty) 
                config.blockchains(b.bid).feedLog 
              else
                config.blockchains(b.bid).feedEvent
            } else config.feed, config.output, ixEvent)(config)
          
          val ixFunc = new InterceptorFunc(b.bid,abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
          val ppFunc = new PipelineETLInterceptFunc(
            if(!b.bid.isEmpty) config.blockchains(b.bid).feedFunc else config.feed, config.output, ixFunc)(config)
          
          val ixMempool = new InterceptorMempool(b.bid,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
          val ppMempool = new PipelineEthInterceptMempool(
            if(!b.bid.isEmpty) config.blockchains(b.bid).feedFunc else config.feed, config.output, ixMempool)(config)

          val ixTx = new InterceptorTx(b.bid,datastoreInterceptions,datastoreScripts,config.alarmsThrottle)
          val ppTx = new PipelineETLInterceptTx(
            if(!b.bid.isEmpty) config.blockchains(b.bid).feedTx else config.feed, config.output,ixTx)(config)
          
          val blockchainIx = List(
            s"${b.bid}.${ixTransaction.entity()}" -> ixTransaction,
            s"${b.bid}.${ixBlock.entity()}" -> ixBlock,
            s"${b.bid}.${ixToken.entity()}" -> ixToken,
            s"${b.bid}.${ixEvent.entity()}" -> ixEvent,
            s"${b.bid}.${ixFunc.entity()}" -> ixFunc,
            s"${b.bid}.${ixMempool.entity()}" -> ixMempool,
            s"${b.bid}.${ixTx.entity()}" -> ixTx,
          )

          (blockchainIx,ppTransaction,ppBlock,ppToken,ppEvent,ppFunc,ppMempool,ppTx)

        })
        
        log.info(s"blockchains: ${blockchainInterceptors.flatMap(_._1).toMap}")

        run( config.host, config.port, config.uri, c,
          Seq(
            (Behaviors.ignore,"",(actor,actorSystem) => new AlarmRoutes("ws")(actorSystem) ),
            (InterceptionRegistry(datastoreInterceptions,
                                  datastoreScripts,
                                  abiStore,
                                  blockchainInterceptors.flatMap(_._1).toMap
                                  ),
              "InterceptionRegistry",(r, ac) => new InterceptionRoutes(r)(ac) )
          )
        )

        blockchainInterceptors.foreach{ case(blockchainIx,ppTransaction,ppBlock,ppToken,ppEvent,ppFunc,ppMempool,ppTx) => 
          // start pipelines
          ppTransaction.run()
          ppBlock.run()
          ppToken.run()
          ppEvent.run()
          ppFunc.run()
          ppMempool.run()
          ppTx.run()
        }
        
        //(r1,Some(ppTx))
        (Future.never,None)
      }

      case "intercept" => {        
        def buildInterceptions(alarms:Seq[String]):Seq[Interception] = {
          val iix = alarms.map(a => { 
              val ix:Interception = a.split("=").toList match {
                case script :: entity :: alarm :: Nil => 
                  Interception(UUID.random, "Ix-1", script, alarm.split(";").toList, entity = entity)
                case script :: alarm :: Nil => 
                  Interception(UUID.random, "Ix-1", script, alarm.split(";").toList, entity = "transaction")
                case alarm :: Nil => 
                  Interception(UUID.random, "Ix-2", "script-1.js", alarm.split(";").toList, entity = "transaction")
                case _ => 
                  Interception(UUID.random, "Ix-3", "script-1.js", List("stdout://"), entity = "transaction")
              }
              ix
            }
          )
          Console.err.println(s"Interceptors: ${iix}")
          iix
        }

        val pp = config.entity match {
          case "block" =>
            new PipelineETLInterceptBlock(config.feed,config.output, 
                new InterceptorBlock(config.bid.head,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))
          case "transaction" =>
            new PipelineETLInterceptTransaction(config.feed,config.output, 
                new InterceptorTransaction(config.bid.head,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))
          case "token" | "transfer" =>
            new PipelineETLInterceptTokenTransfer(config.feed,config.output, 
                new InterceptorTokenTransfer(config.bid.head,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)                          
          case "erc20" =>
            // not useful Interceptor, only for Testing
            new PipelineETLInterceptTx(config.feed,config.output, 
                new InterceptorERC20(config.bid.head,abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)
          case "event" | "log" =>
            new PipelineETLInterceptEvent(config.feed,config.output, 
                new InterceptorEvent(config.bid.head,abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)
          case "func" =>
            new PipelineETLInterceptFunc(config.feed,config.output, 
                new InterceptorFunc(config.bid.head,abiStore,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))(config)
          case "mempool" =>
            new PipelineEthInterceptMempool(config.feed,config.output, 
                new InterceptorMempool(config.bid.head,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))
          
          case "tx" =>
            new PipelineETLInterceptTx(config.feed,config.output, 
                new InterceptorTx(config.bid.head,datastoreInterceptions,datastoreScripts,config.alarmsThrottle,buildInterceptions(config.alarms)))
        }

        (pp.run(),Some(pp))
      }
    }
    
    Console.err.println(s"r=${r},pp=${pp}")

    r match {
      case a:Awaitable[_] => {
        val rr = Await.result(a,FiniteDuration(300,TimeUnit.MINUTES))
        Console.err.println(s"rr: ${rr}")
      }
      case akka.NotUsed => 
    }

    //Console.err.println(s"Result: ${pp.map(_.countObj.value)}")
    Console.err.println(s"Result: ${r}")
    sys.exit(0)
  }
}