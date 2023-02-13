package io.syspulse.haas.intercept.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.haas.intercept.script._
import io.syspulse.haas.intercept.script.Script.ID

class ScriptStoreMem extends ScriptStore {
  val log = Logger(s"${this}")
  
  var scripts: Map[ID,Script] = Map()

  def all:Seq[Script] = scripts.values.toSeq

  def size:Long = scripts.size

  def +(s:Script):Try[ScriptStore] = { 
    scripts = scripts + (s.id -> s)
    log.info(s"${s}")
    Success(this)
  }

  def ?(id:ID):Try[Script] = scripts.get(id) match {
    case Some(s) => Success(s)
    case None => Failure(new Exception(s"not found: ${id}"))
  }

  def ??(txt:String):List[Script] = {    
    scripts.values.filter(v => {
        v.name.toLowerCase.matches(txt.toLowerCase + ".*") ||
        v.desc.map(_.toLowerCase.matches(txt.toLowerCase + ".*")).getOrElse(false)        
      }
    ).toList
  }

}
