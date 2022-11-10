package io.syspulse.haas.ingest.eth.script

import scala.util.Random
import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.dsl.JS
import java.time.LocalDateTime
import java.time.ZonedDateTime

import scala.util.{Success,Failure,Try}

import io.syspulse.haas.ingest.eth.alarm.UserAlarm

abstract class Script(src:String,name:String,id:UUID) {
  protected val log = Logger(s"${this.getClass()}")

  def run(data:Map[String,Any]):Option[Any]
}

class ScriptJS(src:String, name:String = "",id:UUID=UUID.random) extends Script(src,name,id) {  
  val js = new JS(src)
  
  override def run(data:Map[String,Any]):Option[Any] = {    
    val r = Option(js.run(data))
    r
  }
}


object Script {
  val log = Logger(s"${this.getClass()}")
  
  def load(file:String):Try[Script] = {
    //log.info(s"script='${script}'")
    log.info(s"loading script: ${file}...")
    try {
      val src = scala.io.Source.fromFile(file).getLines().mkString("\n")
      log.info(s"script='${src}'")
      Success(new ScriptJS(src))
    } catch {
      case e:Exception => log.error(s"could not load script: ${file}",e); Failure(e)
    }
    
  }

  def apply(script:String,name:String = ""):Try[Script] = {    
    script.trim.split("://").toList match {
      case "file" :: fileName :: Nil => load(fileName)
      case src :: Nil => Success(new ScriptJS(src))
    }    
  }
}
