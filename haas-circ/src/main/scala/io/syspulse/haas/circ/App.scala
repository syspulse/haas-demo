package io.syspulse.haas.circ

import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.Await

import io.syspulse.skel.FutureAwaitable._
import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.jvm.uuid._

import io.syspulse.haas.circ._
import io.syspulse.haas.circ.store._
import io.syspulse.haas.circ.server.CirculationSupplyRoutes
import io.syspulse.haas.circ.client.CirculationSupplyClientHttp
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import io.syspulse.haas.core.Defaults

case class Config(  
  host:String="0.0.0.0",
  port:Int=8080,
  uri:String = "/api/v1/circ",
  httpZip:String = "",
  
  cgUri:String = "",  
  limit:Long = 0L,
  freq: Long = 0L,
  logFile:String = "",   

  tokensDefault:Seq[String] = Defaults.TOKEN_SET.keys.toSeq,
      
  datastore:String = "dir://",

  syslogBus:String = "kafka://localhost:9092",
  syslogChannel:String = "sys.notify",

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
      new ConfigurationArgs(args,"haas-circ","",
        ArgString('h', "http.host",s"listen host (def: ${d.host})"),
        ArgInt('p', "http.port",s"listern port (def: ${d.port})"),
        ArgString('u', "http.uri",s"api uri (def: ${d.uri})"),
        
        ArgString('_', "http.zip",s"Compress large response (def: ${d.httpZip})"),
        ArgString('_', "tokens.default",s"Default token set (def: ${d.tokensDefault.mkString(",")})"),
        
        ArgString('d', "datastore",s"datastore [mem://,dir://store] (def: ${d.datastore})"),

        ArgString('_', "syslog.uri",s"Syslog Bus (kafka:// and syslog://) (def: ${d.syslogBus})"),
        ArgString('_', "syslog.channel",s"Syslog OID (def: ${d.syslogChannel})"),
        
        ArgCmd("server","Server"),
        ArgCmd("client","Client"),
        ArgParam("<params>","")
      ).withExit(1)
    ))

    val config = Config(
      host = c.getString("http.host").getOrElse(d.host),
      port = c.getInt("http.port").getOrElse(d.port),
      uri = c.getString("http.uri").getOrElse(d.uri),

      httpZip = c.getString("http.zip").getOrElse(d.httpZip),
      tokensDefault = c.getListString("tokens.default",d.tokensDefault),
      datastore = c.getString("datastore").getOrElse(d.datastore),

      syslogBus = c.getString("syslog.uri").getOrElse(Configuration.withEnv(d.syslogBus)),
      syslogChannel = c.getSmartString("syslog.channel").getOrElse(d.syslogChannel),

      cmd = c.getCmd().getOrElse(d.cmd),
      params = c.getParams(),
    )

    println(s"Config: ${config}")

    val store = config.datastore.split("://").toList match {
      // case "mysql" | "db" => new CirculationSupplyStoreDB(c,"mysql")
      // case "postgres" => new CirculationSupplyStoreDB(c,"postgres")
      case "mem" :: _ => new CirculationSupplyStoreMem
      case "dir" :: dir :: Nil => new CirculationSupplyStoreDir(dir)
      case "dir" :: Nil => new CirculationSupplyStoreDir()
      case _ => {
        Console.err.println(s"Uknown datastore: '${config.datastore}'")
        sys.exit(1)
      }
    }

    config.cmd match {
      case "server" => 
        run( config.host, config.port,config.uri,c,
          Seq(
            (CirculationSupplyRegistry(store)(config),"CirculationSupplyRegistry",(r, ac) => new CirculationSupplyRoutes(r,config)(ac) )
          )
        )
      case "client" => {        
        val host = if(config.host == "0.0.0.0") "localhost" else config.host
        val uri = s"http://${host}:${config.port}${config.uri}"
        val timeout = FiniteDuration(3,TimeUnit.SECONDS)

        val r = 
          config.params match {
            case "get" :: id :: Nil => CirculationSupplyClientHttp(uri)
                .withTimeout(timeout)
                .get(CirculationSupply(id))
                .await()
            case "all" :: Nil => CirculationSupplyClientHttp(uri)
                .withTimeout(timeout)
                .all()
                .await()

            case Nil => CirculationSupplyClientHttp(uri)
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



