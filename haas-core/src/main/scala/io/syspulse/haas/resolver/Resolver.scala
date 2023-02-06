package io.syspulse.haas.core.resolver

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import java.util.concurrent.TimeUnit

trait Resolver[X,I] {
  def resolve(xid:X):Option[I]
  def resolveReverse(id:I):Option[X]
}
