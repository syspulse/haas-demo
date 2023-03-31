package io.syspulse.haas.core.resolver

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import java.util.concurrent.TimeUnit
import io.syspulse.haas.core.Defaults

class ResolverNone() extends Resolver[String,String] {
  def resolve(xid:String):Option[String] = Some(xid)
  def resolveReverse(id:String):Option[String] = Some(id)
}

