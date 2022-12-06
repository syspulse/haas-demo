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
  import CirculationSupplyJson._
  
  var circs: Map[CirculationSupply.ID,immutable.TreeMap[Long,CirculationSupply]] = Map()


  def all:Seq[CirculationSupply] = circs.values.map(_.values.tail).flatten.toSeq

  def size:Long = circs.size

  def ?(id:CirculationSupply.ID):Option[CirculationSupply] = this.?(id,0L,Long.MaxValue).lastOption

  def ?(id:CirculationSupply.ID,ts0:Long,ts1:Long):List[CirculationSupply] = {
    circs.get(id) match {
      case Some(tmap) => 
        val ts2 = if(ts1 == Long.MaxValue) ts1 else ts1 + 1
        tmap.range(ts0,ts2).values.toList
      case None => List()
    }
  }

  def parse(data:String) = {
    try {
      val cs = data.parseJson.convertTo[CirculationSupply]
      Seq(cs)
    } catch {
      case e:Exception => log.error(s"could not parse data: ${data}",e); Seq()
    } 
  }
}
