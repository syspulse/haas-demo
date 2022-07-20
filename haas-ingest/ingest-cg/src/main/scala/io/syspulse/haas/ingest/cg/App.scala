package io.syspulse.haas.ingest.cg

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import scala.concurrent.{Await, ExecutionContext, Future}

import io.syspulse.haas.ingest.cg.store._

case class Config(  
  cgUri:String = "",  
  limit:Long = 0L,
  freq: Long = 0L,
  logFile:String = "",   
  elasticUri:String = "",
  datastore:String = "",
  cmd:String = "",
  params: Seq[String] = Seq(),sinks:Seq[String] = Seq()
)

object App {
  
  def main(args:Array[String]):Unit = {
    println(s"args: '${args.mkString(",")}'")

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"ingest-cg","",
        ArgString('i', "cg.uri","CG uri"),
        ArgString('o', "elastic.uri","Elastic cluster uri"),
        ArgLong('l', "limit","Limit"),
        ArgLong('f', "freq","Frequency"),
        
        ArgString('d', "datastore","datastore [elastic,stdout,file] (def: stdout)"),
        ArgCmd("ingest","Ingest flow"),
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      cgUri = c.getString("cg.uri").getOrElse("http://localhost:8100"),
      elasticUri = c.getString("elastic.uri").getOrElse("http://localhost:5302"),
      limit = c.getLong("limit").getOrElse(10),
      freq = c.getLong("freq").getOrElse(3600),
      
      datastore = c.getString("datastore").getOrElse("stdout"),
      cmd = c.getCmd().getOrElse("ingest"),
      params = c.getParams(),
    )

    println(s"Config: ${config}")

    val store = config.datastore match {
      // case "elastic" => new CgStoreElastic()
      case "stdout" => new CgStoreStdout[Coingecko]
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}'")
        System.exit(1)
        new CgStoreStdout[Coingecko]
      }
    }

    config.cmd match {
      case "ingest" => 
        new CgIngest[Coingecko](config,c).run(store.getSink)
    }
  }
}