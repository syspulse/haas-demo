package io.syspulse.haas.circ.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation
import io.syspulse.haas.circ.serde.CirculationSupplyJson

class CirculationSupplyStoreMem extends CirculationSupplyStore {
  val log = Logger(s"${this}")
  
  var circs: Map[CirculationSupply.ID,CirculationSupply] = Map()

  def all:Seq[CirculationSupply] = circs.values.toSeq

  def size:Long = circs.size

  def +(c:CirculationSupply):Try[CirculationSupplyStore] = {
    circs = circs + (c.id -> c)
    Success(this)
  }

  def ?(id:CirculationSupply.ID,ts0:Long,ts1:Long):Option[CirculationSupply] = {
    circs.get(id) match {
      case Some(cs) => 
        val ts2 = if(ts1 == Long.MaxValue) ts1 else ts1 + 1
        // WARNING: A bit too much objects for sorting
        val history = immutable.SortedSet.from(cs.history).range(Circulation(ts = ts0),Circulation(ts = ts1))
        Some(cs.copy(history = history))
      case None => None
    }
  }

  def findByToken(tid:String,ts0:Long,ts1:Long):Option[CirculationSupply] = {
    circs.values.find(
      c => c.tokenId.toLowerCase == tid.toLowerCase() ||
           c.name.toLowerCase == tid.toLowerCase()
    ) match {
      case Some(cs) => 
        val ts2 = if(ts1 == Long.MaxValue) ts1 else ts1 + 1
        // WARNING: A bit too much objects for sorting
        val history = immutable.SortedSet.from(cs.history).range(Circulation(ts = ts0),Circulation(ts = ts1))
        Some(cs.copy(history = history))
      case None => None
    }
  }

}
