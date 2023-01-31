package io.syspulse.haas.intercept

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

import io.syspulse.haas.intercept.script._
import io.syspulse.skel.crypto.eth.abi.AbiStore

case class Interception(id:Interception.ID, name:String, 
  scriptId:Script.ID, 
  alarm:List[String] = List(),
  uid:Option[UUID] = None, 
  entity:String = "tx",
  aid:Option[AbiStore.ID] = None,
  ts0:Long = System.currentTimeMillis(),
  
  var status:Interception.Status = Interception.STARTED,
  var count:Long = 0L,
  var history:List[InterceptionAlarm] =  List()) extends Ingestable {
  
  def ++(value:Long = 1) = count = count + value

  def remember(alarm:InterceptionAlarm) = {
    if(history.size > Interception.HISTORY_LIMIT)
      history = history.take(Interception.HISTORY_LIMIT - 1)
    
    // add to the head to have it sorted
    history = history.+:(alarm)    
  }
}

object Interception {
  type ID = UUID
  type Status = String

  val STARTED = "started"
  val STOPPED = "stopped"

  val HISTORY_LIMIT = 100
}