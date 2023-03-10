package io.syspulse.haas.intercept.store

import scala.util.{Try,Failure}

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.skel.store.Store

import io.syspulse.haas.intercept.script._
import io.syspulse.haas.intercept.script.Script.ID

trait ScriptStore extends Store[Script,ID] {
  def getKey(s: Script): ID = s.id

  def +(script:Script):Try[ScriptStore]
  def del(id:ID):Try[ScriptStore] = Failure(new UnsupportedOperationException())
  def ?(id:ID):Try[Script] 
  def all:Seq[Script]
  def size:Long

  def ??(txt:String):List[Script]

  def update(id:ID, name:Option[String] = None, desc:Option[String] = None, src:Option[String] = None):Try[Script]

  protected def modify(sc:Script,name:Option[String]=None,desc:Option[String]=None,src:Option[String]=None):Script = {    
    (for {
      u0 <- Some(sc)
      u1 <- Some(if(name.isDefined) u0.copy(name = name.get) else u0)
      u2 <- Some(if(desc.isDefined) u1.copy(desc = desc) else u1)
      u3 <- Some(if(src.isDefined) u2.copy(src = src.get) else u2)
    } yield u3.copy(ts = Some(System.currentTimeMillis))).get    
  }
}
