package io.syspulse.haas.intercept.store

import scala.util.Try

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.skel.store.Store

import io.syspulse.haas.intercept._
import io.syspulse.haas.intercept.Interception.ID

trait InterceptionStore extends Store[Interception,ID] {
  
  def +(ix:Interception):Try[InterceptionStore]
  def -(ix:Interception):Try[InterceptionStore]
  def del(id:ID):Try[InterceptionStore]
  def ?(id:ID):Option[Interception]

  def findByUser(uid:ID):List[Interception]
  def all:Seq[Interception]
  def size:Long

  def ??(txt:String):List[Interception]

  def search(txt:String):List[Interception]
  
  def stop(id:Interception.ID):Option[Interception]
  def start(id:Interception.ID):Option[Interception]

  def remember(ix:Interception,ia:InterceptionAlarm):Option[Interception]

  def flush(ix:Option[Interception]):Try[InterceptionStore] 
}
