package io.syspulse.haas.intercept.script

import scala.util.Random
import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.dsl.JS
import java.time.LocalDateTime
import java.time.ZonedDateTime

import scala.util.{Success,Failure,Try}

abstract class ScriptEngine(id:ScriptEngine.ID,name:String) {
  protected val log = Logger(s"${this.getClass()}")

  def run(src:String,data:Map[String,Any]):Option[Any]
}

class ScriptJS(id:ScriptEngine.ID, name:String = "") extends ScriptEngine(id,name) {  
  val js = new JS()
  
  override def run(src:String,data:Map[String,Any]):Option[Any] = {    
    val r = Option(js.run(src,data))
    r
  }
}


object ScriptEngine {
  val log = Logger(s"${this.getClass()}")

  type ID = String//UUID

  val engines = Map(
    "js" -> new ScriptJS("js","javascript")
  )

  log.info(s"Script Engines: ${engines}")
  
  // def load(file:String):Try[Scriptable] = {
  //   //log.info(s"script='${script}'")
  //   log.info(s"loading script: ${file}...")
  //   try {
  //     val src = scala.io.Source.fromFile(file).getLines().mkString("\n")
  //     log.info(s"script='${src}'")

  //     Success(new ScriptJS(src))

  //   } catch {
  //     case e:Exception => log.error(s"could not load script: ${file}",e); Failure(e)
  //   }
    
  // }

  // def apply(script:String,name:String = ""):Try[Scriptable] = {    
  //   script.trim.split("://").toList match {
  //     case "file" :: fileName :: Nil => load(fileName)
  //     case src :: Nil => Success(new ScriptJS(src))
  //   }    
  // }
}
