package io.syspulse.haas.ingest.eth.script

import scala.util.Random
import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.dsl.JS
import java.time.LocalDateTime
import java.time.ZonedDateTime

import scala.util.{Success,Failure,Try}

import io.syspulse.haas.ingest.eth.alarm.UserAlarm
import io.syspulse.haas.ingest.eth.notify.{NotficationEmail,NotficationPush}

abstract class Script(script:String, name:String = "",id:UUID=UUID.random) {
  protected val log = Logger(s"${this.getClass()}")

  def load(script:String):Try[JS]

  val js = load(script)
  
  def javascript():Try[JS] = js
}

case class TextScript(script:String, name:String="") extends Script(script,name) {
  override def load(script:String):Try[JS] = Success(new JS(script))
}

case class FileScript(file:String, name:String="") extends Script(file,name) {

  override def load(file:String):Try[JS] = {
    //log.info(s"script='${script}'")
    log.info(s"loading script: ${file}...")
    try {
      val scriptText = scala.io.Source.fromFile(file).getLines().mkString("\n")
      log.info(s"script='${scriptText}'")
      Success(new JS(scriptText))
    } catch {
      case e:Exception => log.error(s"could not load script: ${file}",e); Failure(e)
    }
    
  }  
}

object Script {
  val URI_FILE_PREFIX = "file://"
  def apply(script:String,name:String = ""):Script = {
    if(script.trim.startsWith(URI_FILE_PREFIX)) {
      new FileScript(script.stripPrefix(URI_FILE_PREFIX))
    } else {
      new TextScript(script)
    }
  }
}

object Scripts {

  var scripts = Map[Script,List[UserAlarm]](
    Script("if(to_address == '0x41fb81197275db2105a839fce23858dabf86c73c') 'Transfer: '+to_address; else null;","S-0001") -> 
      List( UserAlarm("A-0001",NotficationEmail("user1@test.org")), UserAlarm("A-0002",NotficationPush("user1-PUSH-1")))
  )

  def +(script:String) = {
    scripts = scripts + ( Script(script) -> List( UserAlarm("A-0002",NotficationPush("user1-PUSH-1",enabled = true)) ))
  }

}


