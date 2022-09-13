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

import io.syspulse.haas.ingest.eth.flow.{ PipelineEthTx,PipelineEthBlock,PipelineEthIntercept,PipelineEthBlockTx,PipelineEthToken }
import io.syspulse.haas.ingest.eth.intercept.InterceptorTx
import io.syspulse.haas.ingest.eth.intercept.InterceptorERC20


object App {
  
  def main(args:Array[String]):Unit = {
    Console.err.println(s"args: '${args.mkString(",")}'")

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"ingest-eth","",
                
        ArgString('f', "feed","Input Feed (def: )"),
        ArgString('o', "output","Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log)"),
        ArgString('e', "entity","Ingest entity: (tx,block,block-tx,token)"),

        ArgLong('_', "limit","Limit"),
        ArgLong('_', "freq","Frequency"),
        ArgString('_', "delimiter","""Delimiter characteds (def: '\n'). Usage example: --delimiter=`echo -e $"\r"` """),
        ArgInt('_', "buffer","Frame buffer (Akka Framing) (def: 1M)"),
        ArgLong('_', "throttle","Throttle messages in msec (def: 0)"),

        ArgString('t', "filter","Filter (ex: '')"),
        
        ArgString('d', "datastore","datastore [elastic,stdout,file] (def: stdout)"),

        ArgString('s', "scripts","Scripts to execute on TX (or file uri: file://script.js (multiple with ',')"),
        ArgString('a', "abi","directory with ABI jsons (format: NAME-0xaddress.json"),
        
        ArgCmd("ingest","Ingest pipeline (requires -e <entity>)"),
        ArgCmd("intercept","Intercept pipeline (-s script)"),
        
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      
      feed = c.getString("feed").getOrElse(""),
      output = c.getString("output").getOrElse(""),
      entity = c.getString("entity").getOrElse("tx"),

      limit = c.getLong("limit").getOrElse(0),
      freq = c.getLong("freq").getOrElse(0),
      delimiter = c.getString("delimiter").getOrElse("\n"),
      buffer = c.getInt("buffer").getOrElse(1024*1024),
      throttle = c.getLong("throttle").getOrElse(0L),     

      filter = c.getString("filter").getOrElse("").split(",").map(_.trim).filter(!_.isEmpty()),
      
      datastore = c.getString("datastore").getOrElse("stdout"),

      scripts = c.getString("scripts").getOrElse("").split(",").toSeq,
      abi = c.getString("abi").getOrElse("abi/"),
      
      cmd = c.getCmd().getOrElse("ingest"),
      
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