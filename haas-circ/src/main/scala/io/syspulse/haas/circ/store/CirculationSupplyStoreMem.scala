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
  
  var circs: Map[CirculationSupply.ID,CirculationSupply] = Map()

  def all:Seq[CirculationSupply] = circs.values.toSeq

  def size:Long = circs.size

  def ?(id:CirculationSupply.ID):Option[CirculationSupply] = circs.get(id)

  def parse(data:String) = {
    try {
      val cs = data.parseJson.convertTo[CirculationSupply]
      Seq(cs)
    } catch {
      case e:Exception => log.error(s"could not parse data: ${data}",e); Seq()
    } 
  }
}
