package io.syspulse.haas.supply.store

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

import io.syspulse.haas.supply.{Supply,SupplyData}
import io.syspulse.haas.supply.SupplyJson

class SupplyStoreMem extends SupplyStore {
  val log = Logger(s"${this}")
  
  var supplys: Map[Supply.ID,Supply] = Map()

  def all:Seq[Supply] = supplys.values.toSeq

  def size:Long = supplys.size

  def +(c:Supply):Try[Supply] = {
    supplys = supplys + (c.id -> c)
    Success(c)
  }

  def ?(id:Supply.ID,ts0:Long,ts1:Long):Option[Supply] = {
    supplys.get(id) match {
      case Some(cs) => 
        val ts2 = if(ts1 == Long.MaxValue) ts1 else ts1 + 1
        // WARNING: A bit too much objects for sorting
        val history = immutable.SortedSet.from(cs.history).range(SupplyData(ts = ts0),SupplyData(ts = ts1))
        Some(cs.copy(history = history))
      case None => None
    }
  }

  def findByToken(tid:String,ts0:Long,ts1:Long):Option[Supply] = {
    supplys.values.find(
      c => c.tokenId.toLowerCase == tid.toLowerCase() ||
           c.name.toLowerCase == tid.toLowerCase()
    ) match {
      case Some(cs) => 
        val ts2 = if(ts1 == Long.MaxValue) ts1 else ts1 + 1
        // WARNING: A bit too much objects for sorting
        val history = immutable.SortedSet.from(cs.history).range(SupplyData(ts = ts0),SupplyData(ts = ts1))
        Some(cs.copy(history = history))
      case None => None
    }
  }

}
