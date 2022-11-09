package io.syspulse.haas.circ.store

import scala.util.{Try,Failure}

import scala.collection.immutable

import io.jvm.uuid._

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.store.Store

import io.syspulse.haas.circ.Config

import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation

trait CirculationSupplyStore extends Store[CirculationSupply,Circulation.ID] {  

  def +(yell:CirculationSupply):Try[CirculationSupplyStore] = Failure(new NotImplementedError())
  def -(yell:CirculationSupply):Try[CirculationSupplyStore]= Failure(new NotImplementedError())
  def del(id:Circulation.ID):Try[CirculationSupplyStore]= Failure(new NotImplementedError())

  def ?(id:Circulation.ID):Option[CirculationSupply]
  def all:Seq[CirculationSupply]
  def size:Long

}
