package io.syspulse.haas.holder.store

import scala.util.Try

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.skel.store.Store

import io.syspulse.haas.holder.Config
import io.syspulse.haas.core.Holders
import io.syspulse.haas.core.Holders.ID
import io.syspulse.haas.holder.server.Holderss
import scala.util.Failure

trait HolderStore extends Store[Holders,ID] {
  def getKey(h:Holders):ID = h.token
  
  def +(h:Holders):Try[HolderStore]
  override def del(id:ID):Try[HolderStore]= Failure(new NotImplementedError())

  def all:Seq[Holders]
  def size:Long

  def ???(id:ID,ts0:Option[Long],ts1:Option[Long],from:Option[Int],size:Option[Int],limit:Option[Int]):Holderss

}
