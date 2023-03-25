package io.syspulse.haas.holder

import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.Await

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.haas.holder._
import io.syspulse.haas.holder.store._

import io.jvm.uuid._

import io.syspulse.skel.FutureAwaitable._
import io.syspulse.haas.holder.server.HolderRoutes

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

case class Config(  
  host:String="0.0.0.0",
  port:Int=8080,
  uri:String = "/api/v1/holders",
  
  cgUri:String = "",  
  limit:Long = 0L,
  freq: Long = 0L,
  logFile:String = "",   
    
  datastore:String = "resources://", //"file://store",

  cmd:String = "server",
  params: Seq[String] = Seq(),

)

object App extends skel.Server {
  
  def main(args:Array[String]):Unit = {
    println(s"args: '${args.mkString(",")}'")
    //sys.props.addOne("god" -> "yes")

    val d = Config()
    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"haas-holder","",
        ArgString('h', "http.host",s"listen host (def: ${d.host})"),
        ArgInt('p', "http.port",s"listern port (def: ${d.port})"),
        ArgString('u', "http.uri",s"api uri (def: ${d.uri})"),
        ArgString('d', "datastore",s"datastore uri (def: ${d.datastore})"),
        
        ArgCmd("server","Server"),
        ArgCmd("client","Client"),
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      host = c.getString("http.host").getOrElse(d.host),
      port = c.getInt("http.port").getOrElse(d.port),
      uri = c.getString("http.uri").getOrElse(d.uri),
      
      datastore = c.getString("datastore").getOrElse(d.datastore),

      cmd = c.getCmd().getOrElse(d.cmd),
      params = c.getParams(),
    )

    println(s"Config: ${config}")

    val store = config.datastore.split("://").toList match {
      // case "mysql" | "db" => new HolderStoreDB(c,"mysql")
      // case "postgres" => new HolderStoreDB(c,"postgres")
      case "mem" :: _ => new HolderStoreMem
      case "dir" :: dir :: Nil => new HolderStoreDir(dir)
      case "dir" :: Nil => new HolderStoreDir()
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}'")
        sys.exit(1)
      }
    }

    config.cmd match {
      case "server" => 
        run( config.host, config.port,config.uri,c,
          Seq(
            (HolderRegistry(store),"HolderRegistry",(r, ac) => new HolderRoutes(r)(ac) )
          )
        )
    }
  }
}


