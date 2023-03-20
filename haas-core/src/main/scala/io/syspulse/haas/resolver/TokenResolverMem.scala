package io.syspulse.haas.core.resolver

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import java.util.concurrent.TimeUnit
import io.syspulse.haas.core.Defaults

class TokenResolverMem(datastore:Option[String] = None) extends Resolver[String,String] {
  val log = Logger(s"${this}")

  val default = Defaults.TOKEN_SET

  val store = default ++ default.map{case(k,v) => v -> k} ++ {
    if(!datastore.isDefined) Map[String,String]() else {
      val r = datastore.get.split("\n").filter(!_.isEmpty()).flatMap(_.split(",",-1).toList match {
        case id :: ticker :: Nil => Some(id.trim -> ticker.trim)
        case _ => None
      }).toMap
      r ++ r.map{case(k,v) => v -> k}
    }
  }

  log.info(s"resolver: ${store}")

  def resolve(xid:String):Option[String] = store.get(xid)
  def resolveReverse(id:String):Option[String] = store.get(id.toLowerCase())
}

