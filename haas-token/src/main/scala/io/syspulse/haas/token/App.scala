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

case class Config(  
  host:String="0.0.0.0",
  port:Int=8080,
  uri:String = "/api/v1/token",
  
  cgUri:String = "",  
  limit:Long = 0L,
  freq: Long = 0L,
  logFile:String = "",   
  
  elasticUri:String = "http://localhost:9200",
  elasticUser:String = "",
  elasticPass:String = "",
  elasticIndex:String = "token",
    
  datastore:String = "file://store",

  cmd:String = "server",
  params: Seq[String] = Seq(),

)

object App extends skel.Server {
  
  def main(args:Array[String]):Unit = {
    println(s"args: '${args.mkString(",")}'")
    sys.props.addOne("god" -> "yes")

    val d = Config()
    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"haas-token","",
        ArgString('h', "http.host",s"listen host (def: ${d.host})"),
        ArgInt('p', "http.port",s"listern port (def: ${d.port})"),
        ArgString('u', "http.uri",s"api uri (def: ${d.uri})"),
        ArgString('d', "datastore",s"datastore [elastic,mem,file://store,resource://] (def: ${d.datastore})"),

        ArgString('_', "elastic.uri",s"Elastic uri (def: ${d.elasticUri})"),
        ArgString('_', "elastic.user",s"Elastic user (def: ${d.elasticUser})"),
        ArgString('_', "elastic.pass",s"Elastic pass (def: ${d.elasticPass})"),
        ArgString('_', "elastic.index",s"Elastic Index (def: ${d.elasticIndex})"),
        
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

      elasticUri = c.getString("elastic.uri").getOrElse(d.elasticUri),
      elasticIndex = c.getString("elastic.index").getOrElse(d.elasticIndex),
      elasticUser = c.getString("elastic.user").getOrElse(d.elasticUser),
      elasticPass = c.getString("elastic.pass").getOrElse(d.elasticPass),
      
      cmd = c.getCmd().getOrElse(d.cmd),
      params = c.getParams(),
    )

    println(s"Config: ${config}")

    val store = config.datastore.split("://").toList match {
      // case "mysql" | "db" => new TokenStoreDB(c,"mysql")
      // case "postgres" => new TokenStoreDB(c,"postgres")
      case "mem" :: _ => new TokenStoreMem
      case "file" :: dir :: Nil => new TokenStoreFile(dir)
      case "file" :: Nil => new TokenStoreFile()
      case "resource" :: file :: Nil => new TokenStoreResource(file)
      case "resource" :: Nil => new TokenStoreResource()
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
        val timeout = Duration("3 seconds")

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



