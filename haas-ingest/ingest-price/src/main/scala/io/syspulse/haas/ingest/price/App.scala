package io.syspulse.haas.ingest.price

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import scala.concurrent.{Await, ExecutionContext, Future}

import io.syspulse.haas.ingest.price.flow._
import java.util.concurrent.TimeUnit
import scala.concurrent.Awaitable

case class Config(  
  
  feed:String = "",
  output:String = "",
  
  size:Long = Long.MaxValue,
  limit:Long = Long.MaxValue,
  freq: Long = 0L,
  delimiter:String = "\n",//"\r\n",
  buffer:Int = 1024*1024,
  throttle:Long = 0L,
  throttleSource:Long = 1000L,
  
  entity:String = "cryptocomp",
  priceFormat:String = "price",

  datastore:String = "stdout",
  tokens:Seq[String] = Seq("uniswap","ribbon-finance"),
  tokensPair:Seq[String] = Seq("USD"),
  ingestCron:String = "360", // 10 minutes

  idResolver:String = "",

  cmd:String = "ingest",
  params: Seq[String] = Seq(),
  sinks:Seq[String] = Seq()
)

object App {
  
  def main(args:Array[String]):Unit = {
    Console.err.println(s"args: '${args.mkString(",")}'")

    val d = Config()
    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"ingest-price","",
                
        ArgString('f', "feed",s"Input Feed (def: ${d.feed})"),
        ArgString('o', "output",s"Output file (pattern is supported: data-{yyyy-MM-dd-HH-mm}.log) (def=${d.output})"),

        ArgString('e', "entity",s"Ingest entity: (cryptocomp,price) (def=${d.entity})"),
        ArgString('t', "tokens",s"Tokens filter (ex: 'UNI,ETH', def=${d.tokens})"),
        ArgString('_', "tokens.pair",s"Tokens pair (ex: 'ETH', def=${d.tokensPair})"),
        
        ArgLong('_', "limit",s"Limit (def=${d.limit})"),
        ArgLong('_', "size",s"Size limit for output (def=${d.size})"),
        ArgLong('_', "freq",s"Frequency (def=${d.freq}"),

        ArgString('_', "delimiter",s"""Delimiter characteds (def: ''). Usage example: --delimiter=`echo -e $"\r"` """),
        ArgInt('_', "buffer",s"Frame buffer (Akka Framing) (def: ${d.buffer})"),
        ArgLong('_', "throttle",s"Throttle messages in msec (def: ${d.throttle})"),
        ArgLong('_', "throttle.source",s"Throttle source (e.g. http, (def: ${d.throttleSource}))"),
        
        ArgString('_', "price.format",s"Output formats (price,telemetry), def=${d.priceFormat})"),
        
        ArgString('d', "datastore",s"datastore [elastic,stdout,file] (def: ${d.datastore})"),
        ArgString('_', "ingest.cron",s"Ingest load cron (currently only seconds interval Tick supported) (def: ${d.ingestCron})"),

        ArgString('_', "id.resolver",s"Source ID resovler (def: ${d.idResolver})"),
        
        ArgCmd("ingest",s"Ingest pipeline (requires -e <entity> and/or -t <tokens,>)"),
        
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(      
      feed = c.getString("feed").getOrElse(d.feed),
      output = c.getString("output").getOrElse(d.output),
      
      limit = c.getLong("limit").getOrElse(d.limit),
      size = c.getLong("size").getOrElse(d.size),
      freq = c.getLong("freq").getOrElse(d.freq),
      delimiter = c.getString("delimiter").getOrElse(d.delimiter),
      buffer = c.getInt("buffer").getOrElse(d.buffer),
      throttle = c.getLong("throttle").getOrElse(d.throttle),
      throttleSource = c.getLong("throttle.source").getOrElse(d.throttleSource),

      entity = c.getString("entity").getOrElse(d.entity),
      tokens = c.getListString("tokens",d.tokens),
      tokensPair = c.getListString("tokens.pair",d.tokensPair),
      priceFormat = c.getString("price.format").getOrElse(d.priceFormat),

      datastore = c.getString("datastore").getOrElse(d.datastore),

      ingestCron = c.getString("ingest.cron").getOrElse(d.ingestCron),

      idResolver = c.getString("id.resolver").getOrElse(d.idResolver),
      
      cmd = c.getCmd().getOrElse(d.cmd),      
      params = c.getParams(),
    )

    Console.err.println(s"Config: ${config}")

    config.cmd match {
      case "ingest" => {
        val pp = config.entity match {
          case "cryptocomp" => new PipelineCryptoCompTerse(config.feed,config.output)(config)
          case "cryptocomp-full" => new PipelineCryptoCompFull(config.feed,config.output)(config)

          case "coingecko" => new PipelineCoinGecko(config.feed,config.output)(config)

          case "chainlink" => new PipelineChainlink(config.feed,config.output)(config)
          
          // internal format
          case "price" => new PipelinePricePrice(config.feed,config.output)(config)
        }
                  
        val r = pp.run()
        println(s"r=${r}")
        r match {
          case a:Awaitable[_] => {
            val rr = Await.result(a,Duration.Inf)
            Console.err.println(s"result: ${rr}")
          }
          case akka.NotUsed => 
            
        }

        Console.err.println(s"Tokens: ${pp.countInput},${pp.countObj},${pp.countOutput}")
        sys.exit(0)        
      }

    }
  }
}