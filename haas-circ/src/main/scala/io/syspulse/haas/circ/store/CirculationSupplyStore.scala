package io.syspulse.haas.circ.store

import scala.util.{Try,Failure}

import scala.collection.immutable

import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.store.Store

import io.syspulse.haas.circ.Config

import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation

trait CirculationSupplyStore extends Store[CirculationSupply,CirculationSupply.ID] {  

  def +(c:CirculationSupply):Try[CirculationSupplyStore]
  
  def -(c:CirculationSupply):Try[CirculationSupplyStore]= Failure(new NotImplementedError())
  def del(id:CirculationSupply.ID):Try[CirculationSupplyStore]= Failure(new NotImplementedError())

  def ?(id:CirculationSupply.ID,ts0:Long,ts1:Long):Option[CirculationSupply]

  def ?(id:CirculationSupply.ID):Option[CirculationSupply] = this.?(id,0L,Long.MaxValue).lastOption
  
  def all:Seq[CirculationSupply]
  def size:Long

}
