package io.syspulse.haas.circ.store

import scala.util.{Try,Failure}

import scala.collection.immutable

import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.store.Store

import io.syspulse.haas.circ.Config

import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation
import io.syspulse.haas.core.Defaults
import scala.collection.SortedSet

trait CirculationSupplyStore extends Store[CirculationSupply,CirculationSupply.ID] {  

  def +(c:CirculationSupply):Try[CirculationSupplyStore]

  def -(c:CirculationSupply):Try[CirculationSupplyStore]= Failure(new NotImplementedError())
  def del(id:CirculationSupply.ID):Try[CirculationSupplyStore]= Failure(new NotImplementedError())

  def ?(id:CirculationSupply.ID,ts0:Long,ts1:Long):Option[CirculationSupply]
  def findByToken(tid:String,ts0:Long,ts1:Long):Option[CirculationSupply]

  def ?(id:CirculationSupply.ID):Option[CirculationSupply] = this.last(id)

  def last(id:CirculationSupply.ID):Option[CirculationSupply] = this.?(id,0L,Long.MaxValue).lastOption
  
  def all:Seq[CirculationSupply]
  def size:Long

  def lastByToken(tid:String):Option[CirculationSupply] = {
    this.findByToken(tid,0L,Long.MaxValue).map(cs => cs.copy(history = SortedSet(cs.history.last)))
  }

  def lastByTokens(tokens:Seq[String] = Defaults.TOKEN_SET,from:Int=0,size:Int=10):Seq[CirculationSupply] = {
    tokens.flatMap( tid => {
      lastByToken(tid) match {
        case Some(c) => Some(c)
        // generate emtpy supplies to have a default list
        case None => Some(CirculationSupply(id = UUID.fromByteArray(Array.fill(16){0},0),tokenId = tid, name="")) 
      }
    }).drop(from).take(size)
  }
}
