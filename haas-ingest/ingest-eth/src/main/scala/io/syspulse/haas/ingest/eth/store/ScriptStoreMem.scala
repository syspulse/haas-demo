package io.syspulse.haas.ingest.eth.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.haas.ingest.eth.script._
import io.syspulse.haas.ingest.eth.script.Script.ID

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

  def ?(id:ID):Option[Script] = {
    scripts.get(id)
  }

  def ??(txt:String):List[Script] = {    
    scripts.values.filter(v => {
        v.name.toLowerCase.matches(txt.toLowerCase) 
        //|| 
        //(v.contractAddress.isDefined && v.contractAddress.get.toLowerCase.matches(txt.toLowerCase))
      }
    ).toList
  }

}
