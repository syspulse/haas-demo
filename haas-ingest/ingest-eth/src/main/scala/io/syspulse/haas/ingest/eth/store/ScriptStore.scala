package io.syspulse.haas.ingest.eth.store

import scala.util.{Try,Failure}

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.skel.store.Store

import io.syspulse.haas.ingest.eth.script._
import io.syspulse.haas.ingest.eth.script.Script.ID

trait ScriptStore extends Store[Script,ID] {
  
  def +(script:Script):Try[ScriptStore]
  def -(script:Script):Try[ScriptStore] = Failure(new UnsupportedOperationException())
  def del(id:ID):Try[ScriptStore] = Failure(new UnsupportedOperationException())
  def ?(id:ID):Option[Script] 
  def all:Seq[Script]
  def size:Long

  def ??(txt:String):List[Script]
}
