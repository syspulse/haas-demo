package io.syspulse.haas.supply.store

import scala.util.{Try,Failure}

import scala.collection.immutable

import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.store.Store

import io.syspulse.haas.supply.Config

import io.syspulse.haas.supply.Supply
import io.syspulse.haas.core.Defaults
import scala.collection.SortedSet
import scala.util.Success

trait SupplyStore extends Store[Supply,Supply.ID] {  
  def getKey(c: Supply): Supply.ID = c.id
  def +(c:Supply):Try[Supply]

  def del(id:Supply.ID):Try[Supply.ID]= Failure(new NotImplementedError())

  def ?(id:Supply.ID,ts0:Long,ts1:Long):Option[Supply]
  def findByToken(tid:String,ts0:Long,ts1:Long):Option[Supply]

  def ?(id:Supply.ID):Try[Supply] = this.last(id) match {
    case Some(y) => Success(y)
    case None => Failure(new Exception(s"not found: ${id}"))
  }

  def last(id:Supply.ID):Option[Supply] = this.?(id,0L,Long.MaxValue).lastOption
  
  def all:Seq[Supply]
  def size:Long

  def lastByToken(tid:String):Option[Supply] = {
    this.findByToken(tid,0L,Long.MaxValue).map(cs => cs.copy(history = SortedSet(cs.history.last)))
  }

  def lastByTokens(tokens:Seq[String],from:Int=0,size:Int=10):Seq[Supply] = {
    tokens.flatMap( tid => {
      lastByToken(tid) match {
        case Some(c) => Some(c)
        // generate emtpy supplies to have a default list
        case None => Some(Supply(id = UUID.fromByteArray(Array.fill(16){0},0),tokenId = tid, name="")) 
      }
    }).drop(from).take(size)
  }
}
