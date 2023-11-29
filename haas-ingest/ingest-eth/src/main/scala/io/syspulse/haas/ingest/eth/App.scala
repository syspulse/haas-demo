package io.syspulse.haas.ingest.eth

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

import io.syspulse.skel.ingest.flow.Pipeline
import io.syspulse.haas.ingest.eth.flow._

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
        ArgString('_', "feed.tx",s"Tx Feed (def: ${d.feedTx})"),
        ArgString('_', "feed.block",s"Block Feed (def: ${d.feedBlock})"),
        ArgString('_', "feed.transfer",s"Token Transfer Feed (def: ${d.feedTransfer})"),
        ArgString('_', "feed.log",s"EventLog Feed (def: ${d.feedLog})"),
        ArgString('_', "feed.mempool",s"Mempool Feed (def: ${d.feedMempool})"),

        ArgString('o', "output",s"Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log) def=${d.output}"),        
        ArgString('_', "output.tx",s"Tx Feed (def: ${d.outputTx})"),
        ArgString('_', "output.block",s"Block Feed (def: ${d.outputBlock})"),
        ArgString('_', "output.transfer",s"Token Transfer Feed (def: ${d.outputTransfer})"),
        ArgString('_', "output.log",s"EventLog Feed (def: ${d.outputLog})"),
        ArgString('_', "output.mempool",s"Mempool Feed (def: ${d.outputMempool})"),

        ArgString('e', "entity",s"Ingest entity: (tx,block,block-tx,transfer,log|event) def=${d.entity}"),

        ArgLong('_', "limit",s"Limit for entities to output (def=${d.limit})"),
        ArgLong('_', "size",s"Size limit for output (def=${d.size})"),

        ArgLong('_', "freq",s"Frequency (def=${d.freq}"),
        ArgString('_', "delimiter","""Delimiter characteds (def: '\n'). Usage example: --delimiter=`echo -e $"\r"` """),
        ArgInt('_', "buffer",s"Frame buffer (Akka Framing) (def: ${d.buffer})"),
        ArgLong('_', "throttle",s"Throttle messages in msec (def: ${d.throttle})"),

        ArgString('t', "filter",s"Filter (def='${d.filter}')"),
        
        ArgString('d', "datastore",s"datastore for intercetpions (def: ${d.datastore})"),
        ArgString('_', "abi",s"directory with ABI jsons (format: NAME-0xaddress.json) (def=${d.abi}"),

        ArgString('_', "ingest.cron",s"Ingest load cron (currently only seconds interval Tick supported) (def: ${d.ingestCron})"),
        ArgLong('_', "throttle.source",s"Throttle source (e.g. http, (def: ${d.throttleSource}))"),
        ArgString('_', "block",s"Ingest from this block (def: ${d.block})"),
        ArgString('_', "block.end",s"Ingest until this block (def: ${d.blockEnd})"),
        ArgInt('l', "lag",s"Ingest lag (def: ${d.blockLag})"),
        
        ArgCmd("server",s"Server"),
        ArgCmd("ingest",s"Ingest pipeline (requires -e <entity>)"),
        ArgCmd("intercept",s"Intercept pipeline (-s script)"),
        
        ArgParam("<params>",""),
        ArgLogging()
      ).withExit(1)
    )).withLogging()

    val config = Config(
      host = c.getString("http.host").getOrElse(d.host),
      port = c.getInt("http.port").getOrElse(d.port),
      uri = c.getString("http.uri").getOrElse(d.uri),
      
      feed = c.getString("feed").getOrElse(d.feed),
      output = c.getString("output").getOrElse(d.output),
      entity = c.getListString("entity",d.entity),
      
      feedBlock = c.getString("feed.block").getOrElse(d.feedBlock),
      feedTransaction = c.getString("feed.transction").getOrElse(d.feedTransaction),
      feedTransfer = c.getString("feed.transfer").getOrElse(d.feedTransfer),
      feedLog = c.getString("feed.log").getOrElse(d.feedLog),
      feedMempool = c.getString("feed.mempool").getOrElse(d.feedMempool),
      feedTx = c.getString("feed.tx").getOrElse(d.feedTx),

      outputBlock = c.getString("output.block").getOrElse(d.outputBlock),
      outputTx = c.getString("output.tx").getOrElse(d.outputTx),
      outputTransfer = c.getString("output.transfer").getOrElse(d.outputTransfer),
      outputLog = c.getString("output.log").getOrElse(d.outputLog),
      outputMempool = c.getString("output.mempool").getOrElse(d.outputMempool),

      limit = c.getLong("limit").getOrElse(d.limit),
      size = c.getLong("size").getOrElse(d.size),

      freq = c.getLong("freq").getOrElse(d.freq),
      delimiter = c.getString("delimiter").getOrElse(d.delimiter),
      buffer = c.getInt("buffer").getOrElse(d.buffer),
      throttle = c.getLong("throttle").getOrElse(d.throttle),     

      filter = c.getListString("filter",d.filter),
      
      datastore = c.getString("datastore").getOrElse(d.datastore),
      
      abi = c.getString("abi").getOrElse(d.abi),

      throttleSource = c.getLong("throttle.source").getOrElse(d.throttleSource),
      ingestCron = c.getString("ingest.cron").getOrElse(d.ingestCron),
      block = c.getString("block").getOrElse(d.block),
      blockEnd = c.getString("block.end").getOrElse(d.blockEnd),
      blockLag = c.getInt("lag").getOrElse(d.blockLag),
      
      cmd = c.getCmd().getOrElse(d.cmd),
      
      params = c.getParams(),
    )

    Console.err.println(s"Config: ${config}")

    def orf(config:Config,feed1:String,feed2:String,out1:String,out2:String) = {
      val c0 = config
      val c1 = if(feed1!="") c0.copy(feed = feed1) else c0.copy(feed = feed2)
      val c2 = if(out1!="") c1.copy(output = out1) else c1.copy(output = out2)
      c2
    }
    
    val (r,pp) = config.cmd match {
      case "ingest" => {
        val pp:Seq[PipelineEth[_,_,_]] = config.entity.flatMap( e => e match {

          // ethereum_etl 
          case "block" | "block.etl" =>
            Some(new etl.PipelineBlock(orf(config,config.feedBlock,config.feed,config.outputBlock,config.output)))

          case "transaction" | "transaction.etl" =>
            Some(new etl.PipelineTransaction(orf(config,config.feedTransaction,config.feed,config.outputTx,config.output)))
          
          case "transfer" | "token" | "transfer.etl" | "token.etl" =>
            Some(new etl.PipelineTokenTransfer(orf(config,config.feedTransfer,config.feed,config.outputTransfer,config.output)))

          case "log" | "event" | "log.etl" | "event.etl" => 
            Some(new etl.PipelineLog(orf(config,config.feedLog,config.feed,config.outputLog,config.output)))

              
          case "mempool" => 
            Some(new PipelineMempool(orf(config,config.feedMempool,config.feed,config.outputMempool,config.output)))

          case "tx" | "tx.etl" =>
            Some(new etl.PipelineTx(orf(config,config.feedTx,config.feed,config.outputTx,config.output)))

          // Lake stored
          case "block.lake" =>
            Some(new lake.PipelineBlock(orf(config,config.feedBlock,config.feed,config.outputBlock,config.output)))
          case "transaction.lake" =>
            Some(new lake.PipelineTransaction(orf(config,config.feedTransaction,config.feed,config.outputTransaction,config.output)))
          case "transfer.lake" | "token.lake" =>
            Some(new lake.PipelineTokenTransfer(orf(config,config.feedTransfer,config.feed,config.outputTransfer,config.output)))
          case "log.lake" | "event.lake" =>
            Some(new lake.PipelineEvent(orf(config,config.feedLog,config.feed,config.outputLog,config.output)))
          case "tx.lake" =>
            Some(new lake.PipelineTx(orf(config,config.feedTx,config.feed,config.outputTx,config.output)))

          // RPC
          case "block.rpc" =>
            Some(new rpc3.PipelineBlock(orf(config,config.feedBlock,config.feed,config.outputBlock,config.output)))
          
          // Transaction and Tx return the same !!
          case "transaction.rpc" | "tx.rpc" =>
            Some(new rpc3.PipelineTx(orf(config,config.feedTransaction,config.feed,config.outputTransaction,config.output)))

          case _ => 
            Console.err.println(s"Uknown entity: '${e}'");
            sys.exit(1)
            None
        })

        // start all pipelines
        val ppr = pp.map( _.run())

        (ppr.head,Some(pp.head))
                
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