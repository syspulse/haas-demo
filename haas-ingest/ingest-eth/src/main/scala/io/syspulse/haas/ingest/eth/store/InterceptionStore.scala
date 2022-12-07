package io.syspulse.haas.ingest.eth.store

import scala.util.Try

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.skel.store.Store

import io.syspulse.haas.ingest.eth.intercept._
import io.syspulse.haas.ingest.eth.intercept.Interception.ID

trait InterceptionStore extends Store[Interception,ID] {
  
  def +(ix:Interception):Try[InterceptionStore]
  def -(ix:Interception):Try[InterceptionStore]
  def del(id:ID):Try[InterceptionStore]
  def ?(id:ID):Option[Interception]
  def all:Seq[Interception]
  def size:Long

  def ??(txt:String):List[Interception]

  def search(txt:String):List[Interception]
  
  def stop(id:Interception.ID):Option[Interception]
  def start(id:Interception.ID):Option[Interception]
}
