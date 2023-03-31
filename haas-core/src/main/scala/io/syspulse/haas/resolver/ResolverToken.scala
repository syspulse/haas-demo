package io.syspulse.haas.core.resolver

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import java.util.concurrent.TimeUnit
import io.syspulse.haas.core.Defaults

class ResolverToken(uri:String) extends Resolver[String,String] {
  val log = Logger(s"${this}")

  //val default = Defaults.TOKEN_SET
  val fileRegxp = """file://(\S+)""".r

  val store = {
    //default ++ default.map{case(k,v) => v -> k} ++ 
    val store1 = (uri match {
        case fileRegxp(file) => 
          os.read(os.Path(file,os.pwd))
        case _ => 
          uri
    })
    .split("\n")
    .filter(!_.isEmpty())
    .flatMap(_.split(",",-1).toList match {
      case id :: ticker :: Nil => Some(id.trim -> ticker.trim.toUpperCase)
      case _ => None
    })
    .toMap

    store1.map { case(id,ticker) =>
      ticker -> id
    } ++ store1
  }
  

  log.debug(s"resolver: ${store}")

  def resolve(xid:String):Option[String] = store.get(xid)
  def resolveReverse(id:String):Option[String] = store.get(id.toLowerCase())
}

