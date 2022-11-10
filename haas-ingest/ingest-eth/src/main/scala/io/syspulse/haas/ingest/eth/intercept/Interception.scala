package io.syspulse.haas.ingest.eth.intercept

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

case class Interception(ts:Long,scriptId:String,block:Long,tx:String,output:String,alarm:List[String] = List()) extends Ingestable

object InterceptionJson extends DefaultJsonProtocol with NullOptions {
  implicit val jf_i = jsonFormat6(Interception)
}
