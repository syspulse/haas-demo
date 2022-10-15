package io.syspulse.haas.ingest.eth

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import java.util.concurrent.TimeUnit
import scala.concurrent.Awaitable
import scala.concurrent.{Await, ExecutionContext, Future}

import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.haas.ingest.eth.flow.{ PipelineEthTx,PipelineEthBlock,PipelineEthIntercept,PipelineEthBlockTx,PipelineEthToken,PipelineEthLog }
import io.syspulse.haas.ingest.eth.intercept.InterceptorTx
import io.syspulse.haas.ingest.eth.intercept.InterceptorERC20


object App {
  
  def main(args:Array[String]):Unit = {
    Console.err.println(s"args: '${args.mkString(",")}'")

    val d = Config()

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"ingest-eth","",
                
        ArgString('f', "feed",s"Input Feed (def: ${d.feed})"),
        ArgString('o', "output",s"Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log) def=${d.output}"),
        ArgString('e', "entity",s"Ingest entity: (tx,block,block-tx,token,log) def=${d.entity}"),

        ArgLong('_', "limit",s"Limit for entities to output (def=${d.limit})"),
        ArgLong('_', "size",s"Size limit for output (def=${d.size})"),

        ArgLong('_', "freq",s"Frequency (def=${d.freq}"),
        ArgString('_', "delimiter",s"""Delimiter characteds (def: '\n'). Usage example: --delimiter=`echo -e $"\r"` """),
        ArgInt('_', "buffer",s"Frame buffer (Akka Framing) (def: ${d.buffer})"),
        ArgLong('_', "throttle",s"Throttle messages in msec (def: ${d.throttle})"),

        ArgString('t', "filter",s"Filter (def='${d.filter}')"),
        
        ArgString('d', "datastore",s"datastore (def: ${d.datastore})"),

        ArgString('s', "scripts",s"Scripts to execute on TX (or file uri: file://script.js (multiple with ',') (def=${d.scripts})"),
        ArgString('a', "abi",s"directory with ABI jsons (format: NAME-0xaddress.json) (def=${d.abi}"),
        
        ArgCmd("ingest",s"Ingest pipeline (requires -e <entity>)"),
        ArgCmd("intercept",s"Intercept pipeline (-s script)"),
        
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      
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

      scripts = c.getListString("scripts",d.filter),
      abi = c.getString("abi").getOrElse(d.abi),
      
      cmd = c.getCmd().getOrElse(d.cmd),
      
      params = c.getParams(),
    )

    Console.err.println(s"Config: ${config}")

    val (pp,r) = config.cmd match {
      case "ingest" => {
        val pp = config.entity match {
          case "tx" =>
            new PipelineEthTx(config.feed,config.output)(config)
                    
          case "block" =>
            new PipelineEthBlock(config.feed,config.output)(config)            
          
          case "block-tx" =>
            new PipelineEthBlockTx(config.feed,config.output)(config)

          case "token" =>
            new PipelineEthToken(config.feed,config.output)(config)

          case "log" =>
            new PipelineEthLog(config.feed,config.output)(config)

          case _ =>  Console.err.println(s"Uknown entity: '${config.entity}'"); sys.exit(1)
        } 

        (pp,pp.run())
        
      }
    
      case "intercept" => {
        val pp = config.entity match {
          case "tx" =>
            new PipelineEthIntercept(config.feed,config.output, new InterceptorTx(config))(config)
          case "erc20" =>
            new PipelineEthIntercept(config.feed,config.output, new InterceptorERC20(config))(config)
        }
        (pp,pp.run())
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

    Console.err.println(s"Result: ${pp.countObj}")
    sys.exit(0)
  }
}