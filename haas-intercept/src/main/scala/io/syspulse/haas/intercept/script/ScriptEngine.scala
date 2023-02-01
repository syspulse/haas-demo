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

  def run(src:String,data:Map[String,Any]):Try[Any]
}

class ScriptJS(id:ScriptEngine.ID, name:String = "") extends ScriptEngine(id,name) {  
  val js = new JS()
  
  override def run(src:String,data:Map[String,Any]):Try[Any] = {    
    try {
      Success(js.run(src,data))
    } catch {
      // case e:jdk.nashorn.internal.runtime.ECMAException => Failure(e)
      // case e:javax.script.ScriptException => Failure(e)
      // case e:Exception => Failure(e)
      case e:Throwable => Failure(e)
    }
  }
}


object ScriptEngine {
  val log = Logger(s"${this.getClass()}")

  type ID = String//UUID

  val engines = Map(
    "js" -> new ScriptJS("js","javascript")
  )

  log.info(s"Script Engines: ${engines}")

}
