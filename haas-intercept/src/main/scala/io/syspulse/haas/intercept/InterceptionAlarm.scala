package io.syspulse.haas.intercept

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Random

import com.typesafe.scalalogging.Logger

import spray.json._
import DefaultJsonProtocol._

import java.time.LocalDateTime
import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success
import io.syspulse.skel.Ingestable

case class InterceptionAlarm(
    ts:Long,
    iid:Interception.ID,
    bid:Option[String],
    block:Long,
    hash:String,
    output:String,
    alarm:List[String] = List()) extends Ingestable

