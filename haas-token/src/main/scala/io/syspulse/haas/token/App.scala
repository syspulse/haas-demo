package io.syspulse.haas.token

import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.Await

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.haas.token._
import io.syspulse.haas.token.store._

import io.jvm.uuid._

import io.syspulse.skel.FutureAwaitable._
import io.syspulse.haas.token.server.TokenRoutes

import io.syspulse.haas.token.client.TokenClientHttp
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

case class Config(  
  host:String="0.0.0.0",
  port:Int=8080,
  uri:String = "/api/v1/token",
  
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
      new ConfigurationArgs(args,"haas-token","",
        ArgString('h', "http.host",s"listen host (def: ${d.host})"),
        ArgInt('p', "http.port",s"listern port (def: ${d.port})"),
        ArgString('u', "http.uri",s"api uri (def: ${d.uri})"),
        ArgString('d', "datastore",s"datastore [elastic://localhost:9200/token, mem, dir://store, file://tokens.json, resources://, resources://file] (def: ${d.datastore})"),
        
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
      // case "mysql" | "db" => new TokenStoreDB(c,"mysql")
      // case "postgres" => new TokenStoreDB(c,"postgres")
      case "mem" :: _ => new TokenStoreMem
      case "file" :: file :: Nil => new TokenStoreFile(file)
      case "file" :: Nil => new TokenStoreFile()
      case "dir" :: dir :: Nil => new TokenStoreDir(dir)
      case "dir" :: Nil => new TokenStoreDir()
      case "resources" :: file :: Nil => new TokenStoreResource(file)
      case "resources" :: Nil => new TokenStoreResource()
      case "elastic" :: uri :: _ => new TokenStoreElastic(uri)
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}'")
        sys.exit(1)
      }
    }

    config.cmd match {
      case "server" => 
        run( config.host, config.port,config.uri,c,
          Seq(
            (TokenRegistry(store),"TokenRegistry",(r, ac) => new TokenRoutes(r)(ac) )
          )
        )

      case "client" => {
        
        val host = if(config.host == "0.0.0.0") "localhost" else config.host
        val uri = s"http://${host}:${config.port}${config.uri}"
        val timeout = FiniteDuration(3,TimeUnit.SECONDS)

        val r = 
          config.params match {
            case "create" :: data => 
              val (id:String,symbol:String,name:String) = data match {
                case id :: symbol :: name :: _ => (id,symbol,name)
                case id :: symbol :: Nil => (id,symbol,"name")
                case id :: Nil => (id,"symbol","name")
                case Nil => ("token","symbol","name")
              }
              TokenClientHttp(uri)
                .withTimeout(timeout)
                .create(id,symbol,name)
                .await()
            case "get" :: id :: Nil => 
              TokenClientHttp(uri)
                .withTimeout(timeout)
                .get(id)
                .await()
            case "search" :: txt :: Nil => 
              TokenClientHttp(uri)
                .withTimeout(timeout)
                .search(txt)
                .await()
            case "all" :: Nil => 
              TokenClientHttp(uri)
                .withTimeout(timeout)
                .all()
                .await()

            case Nil => TokenClientHttp(uri)
                .withTimeout(timeout)
                .all()
                .await()

            case _ => println(s"unknown op: ${config.params}")
          }
        
        println(s"${r}")
        System.exit(0)
      }
    }
  }
}



