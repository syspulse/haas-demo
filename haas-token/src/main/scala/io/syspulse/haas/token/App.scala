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

case class Config(  
  host:String="",
  port:Int=0,
  uri:String = "",
  
  cgUri:String = "",  
  limit:Long = 0L,
  freq: Long = 0L,
  logFile:String = "",   
  
  elasticUri:String = "",
  elasticUser:String = "",
  elasticPass:String = "",
  elasticIndex:String = "",
  expr:String = "",
    
  datastore:String = "",

  tokens:Seq[String] = Seq(),

  cmd:String = "",
  params: Seq[String] = Seq(),

)

object App extends skel.Server {
  
  def main(args:Array[String]):Unit = {
    println(s"args: '${args.mkString(",")}'")

    val c = Configuration.withPriority(Seq(
      new ConfigurationAkka,
      new ConfigurationProp,
      new ConfigurationEnv, 
      new ConfigurationArgs(args,"eth-stream","",
        ArgString('h', "http.host","listen host (def: 0.0.0.0)"),
        ArgInt('p', "http.port","listern port (def: 8080)"),
        ArgString('u', "http.uri","api uri (def: /api/v1/token)"),
        ArgString('d', "datastore","datastore [elastic,mem,cache] (def: mem)"),

        ArgString('_', "elastic.uri","Elastic uri (def: http://localhost:9200)"),
        ArgString('_', "elastic.user","Elastic user (def: )"),
        ArgString('_', "elastic.pass","Elastic pass (def: )"),
        ArgString('_', "elastic.index","Elastic Index (def: token)"),
        
        ArgCmd("server","Command"),
        ArgCmd("client","Command"),
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      host = c.getString("http.host").getOrElse("0.0.0.0"),
      port = c.getInt("http.port").getOrElse(8080),
      uri = c.getString("http.uri").getOrElse("/api/v1/token"),
      datastore = c.getString("datastore").getOrElse("mem"),

      elasticUri = c.getString("elastic.uri").getOrElse("http://localhost:9200"),
      elasticIndex = c.getString("elastic.index").getOrElse("token"),
      elasticUser = c.getString("elastic.user").getOrElse(""),
      elasticPass = c.getString("elastic.pass").getOrElse(""),
      
      cmd = c.getCmd().getOrElse("server"),
      params = c.getParams(),
    )

    println(s"Config: ${config}")

    val store = config.datastore match {
      // case "mysql" | "db" => new TokenStoreDB(c,"mysql")
      // case "postgres" => new TokenStoreDB(c,"postgres")
      case "mem" | "cache" => new TokenStoreMem
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}': using 'mem'")
        new TokenStoreMem
      }
    }

    // config.cmd match {
      // case "server" => 
      //   run( config.host, config.port,config.uri,c,
      //     Seq(
      //       (TokenRegistry(store),"TokenRegistry",(r, ac) => new TokenRoutes(r)(ac) )
      //     )
      //   )

      // case "client" => {
        
      //   val host = if(config.host == "0.0.0.0") "localhost" else config.host
      //   val uri = s"http://${host}:${config.port}${config.uri}"
      //   val timeout = Duration("3 seconds")

      //   val r = 
      //     config.params match {
      //       case "delete" :: id :: Nil => 
      //         TokenClientHttp(uri)
      //           .withTimeout(timeout)
      //           .delete(UUID(id))
      //           .await()
      //       case "create" :: data => 
      //         val (email:String,name:String,eid:String) = data match {
      //           case email :: name :: eid :: _ => (email,name,eid)
      //           case email :: name :: Nil => (email,name,"")
      //           case email :: Nil => (email,"","")
      //           case Nil => ("token-1@mail.com","","")
      //         }
      //         TokenClientHttp(uri)
      //           .withTimeout(timeout)
      //           .create(email,name,eid)
      //           .await()
      //       case "get" :: id :: Nil => 
      //         TokenClientHttp(uri)
      //           .withTimeout(timeout)
      //           .get(UUID(id))
      //           .await()
      //       case "getByEid" :: eid :: Nil => 
      //         TokenClientHttp(uri)
      //           .withTimeout(timeout)
      //           .getByEid(eid)
      //           .await()
      //       case "all" :: Nil => 
      //         TokenClientHttp(uri)
      //           .withTimeout(timeout)
      //           .all()
      //           .await()

      //       case Nil => TokenClientHttp(uri)
      //           .withTimeout(timeout)
      //           .all()
      //           .await()

      //       case _ => println(s"unknown op: ${config.params}")
      //     }
        
      //   println(s"${r}")
      //   System.exit(0)
      // }
    // }
  }
}



