package io.syspulse.haas.ingest.eth.script

import scala.util.Random
import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.dsl.JS
import java.time.LocalDateTime
import java.time.ZonedDateTime

import scala.util.{Success,Failure,Try}

case class Script(id:Script.ID,typ:String,src:String,name:String) 

object Script {
  type ID = String //UUID
}
