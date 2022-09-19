package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import scala.concurrent.{Await, ExecutionContext, Future}

import io.syspulse.haas.ingest.gecko.flow.{ PipelineCoins,PipelineCoinInfo }
import java.util.concurrent.TimeUnit
import scala.concurrent.Awaitable

case class Config(  
  
  feed:String = "",
  output:String = "",
  
  limit:Long = 0L,
  freq: Long = 0L,
  delimiter:String = "",
  buffer:Int = 0,
  throttle:Long = 0L,
  throttleSource:Long = 0L,
  
  entity:String = "",
  
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
                
        ArgString('f', "feed","Input Feed (def: )"),
        ArgString('o', "output","Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log)"),
        ArgString('e', "entity","Ingest entity: (coin,coins)"),

        ArgLong('_', "limit","Limit"),
        ArgLong('_', "freq","Frequency"),
        ArgString('_', "delimiter","""Delimiter characteds (def: ''). Usage example: --delimiter=`echo -e $"\r"` """),
        ArgInt('_', "buffer","Frame buffer (Akka Framing) (def: 1M)"),
        ArgLong('_', "throttle","Throttle messages in msec (def: 0)"),
        ArgLong('_', "throttle.source","Throttle source (e.g. http, def=1000L)"),

        ArgString('t', "tokens","Tokens filter (ex: 'UNI,ETH')"),
        
        ArgString('d', "datastore","datastore [elastic,stdout,file] (def: stdout)"),
        
        ArgCmd("ingest","Ingest pipeline (requires -e <entity> and/or -t <tokens,>)"),
        
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      
      feed = c.getString("feed").getOrElse(""),
      output = c.getString("output").getOrElse(""),
      entity = c.getString("entity").getOrElse("coins"),

      limit = c.getLong("limit").getOrElse(0),
      freq = c.getLong("freq").getOrElse(0),
      delimiter = c.getString("delimiter").getOrElse(""),
      buffer = c.getInt("buffer").getOrElse(1024*1024),
      throttle = c.getLong("throttle").getOrElse(0L),
      throttleSource = c.getLong("throttle.source").getOrElse(1000L),

      tokens = c.getListString("tokens"),
      
      datastore = c.getString("datastore").getOrElse("stdout"),
      
      cmd = c.getCmd().getOrElse("ingest"),
      
      params = c.getParams(),
    )

    Console.err.println(s"Config: ${config}")

    config.cmd match {
      case "ingest" => {
        val pp = config.entity match {
          case "coins" =>
            new PipelineCoins(config.feed,config.output)(config)
          case "coin" =>
            new PipelineCoinInfo(config.feed,config.output)(config)
          case _ =>  Console.err.println(s"Uknown entity: '${config.entity}'"); sys.exit(1)
        } 

        val r = pp.run()
        println(s"r=${r}")
        r match {
          case a:Awaitable[_] => {
            val rr = Await.result(a,FiniteDuration(30,TimeUnit.MINUTES))
            Console.err.println(s"result: ${rr}")
          }
          case akka.NotUsed => 
        }

        Console.err.println(s"Tokens: ${pp.countObj}")
        sys.exit(0)
      }

    }
  }
}