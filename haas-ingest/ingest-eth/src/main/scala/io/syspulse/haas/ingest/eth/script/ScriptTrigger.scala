package io.syspulse.haas.ingest.eth.script

import scala.util.Random
import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.dsl.JS
import java.time.LocalDateTime
import java.time.ZonedDateTime

import scala.util.{Success,Failure,Try}

import io.syspulse.haas.ingest.eth.alarm.UserAlarm

class ScriptTrigger(id:String,scriptUri:String) {

  val script = Script(scriptUri)
  
  def getScript():Option[Script] = script.toOption
  def getScriptId(): String = id 
}


