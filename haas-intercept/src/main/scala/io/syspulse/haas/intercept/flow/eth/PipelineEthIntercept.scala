package io.syspulse.haas.intercept.flow.eth

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import spray.json._
import DefaultJsonProtocol._
import java.util.concurrent.TimeUnit

import io.syspulse.haas.intercept.Interceptor
import io.syspulse.haas.intercept.Interception
import io.syspulse.haas.intercept.InterceptionAlarm

trait PipelineEthIntercept[O] {
  def interceptor:Interceptor[O] = ???
  
  def transform(t: O): Seq[InterceptionAlarm] = { 
    interceptor.scan(t)
  }
}

