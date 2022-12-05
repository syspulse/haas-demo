package io.syspulse.haas.ingest.eth.intercept

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Random

import com.typesafe.scalalogging.Logger
import io.jvm.uuid._

import spray.json._
import DefaultJsonProtocol._

import java.time.LocalDateTime
import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success
import io.syspulse.skel.Ingestable

import io.syspulse.haas.ingest.eth.script._

case class Interception(id:Interception.ID, name:String, scriptId:Script.ID, alarm:List[String] = List(),uid:Option[UUID] = None, status:String="started",var count:Long = 0L) extends Ingestable {
  def ++(value:Long = 1) = count = count + value
}

object Interception {
  type ID = UUID
}