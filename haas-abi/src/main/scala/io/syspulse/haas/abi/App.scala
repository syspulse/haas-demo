package io.syspulse.haas.abi

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}

import akka.NotUsed
import scala.concurrent.Awaitable
import scala.concurrent.Await
import java.util.concurrent.TimeUnit
import scala.concurrent.Future

import io.syspulse.skel
import io.syspulse.skel.config._
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.skel.ingest.IngestFlow
import io.syspulse.skel.ingest.flow.Flows

import io.syspulse.skel.crypto.eth.abi._
import io.syspulse.haas.abi.store.AbiRegistry

case class Config(
  host:String="0.0.0.0",
  port:Int=8080,
  uri:String = "/api/v1/abi",

  expr:String = "",
  
  limit:Long = -1,
  feed:String = "",
  output:String = "",
  delimiter:String = "",
  buffer:Int = 1024*1024,
  throttle:Long = 0L,

  datastore:String = "dir://store/",

  cmd:String = "server",
  params: Seq[String] = Seq(),
)

object App extends skel.Server {
  
  def main(args:Array[String]): Unit = {
    Console.err.println(s"args: '${args.mkString(",")}'")

    val d = Config()

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"haas-abi","",
        ArgString('h', "http.host",s"listen host (def: ${d.host})"),
        ArgInt('p', "http.port",s"listern port (def: ${d.port})"),
        ArgString('u', "http.uri",s"api uri (def: ${d.uri})"),
        
        ArgString('d', "datastore",s"Datastore [dir://] (def: ${d.datastore})"),
        
        ArgCmd("server","HTTP Service"),        

        ArgParam("<params>",""),
        ArgLogging()
      ).withExit(1)
    )).withLogging()

    val config = Config(
      host = c.getString("http.host").getOrElse(d.host),
      port = c.getInt("http.port").getOrElse(d.port),
      uri = c.getString("http.uri").getOrElse(d.uri),

      feed = c.getString("feed").getOrElse(d.feed),
      limit = c.getLong("limit").getOrElse(d.limit),
      output = c.getString("output").getOrElse(d.output),

      delimiter = c.getString("delimiter").getOrElse(d.delimiter),
      buffer = c.getInt("buffer").getOrElse(d.buffer),
      throttle = c.getLong("throttle").getOrElse(d.throttle),

      datastore = c.getString("datastore").getOrElse(d.datastore),

      expr = c.getString("expr").getOrElse(d.expr),
      
      cmd = c.getCmd().getOrElse(d.cmd),
      params = c.getParams(),
    )

    Console.err.println(s"Config: ${config}")

    val store:AbiStore = config.datastore.split("://").toList match {
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
    
    store.load()

    val r = config.cmd match {
      case "server" => 
        run( config.host, config.port,config.uri,c,
          Seq(
            (AbiRegistry(store),"AbiRegistry",(r, ac) => new server.AbiRoutes(r)(ac) )
          )
        )
    }
    Console.err.println(s"\n${r}")      
        
  }
}