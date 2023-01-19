package io.syspulse.haas.circ.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.haas.circ.server._
import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation

object CirculationSupplyRegistry {
  val log = Logger(s"${this}")
  
  final case class GetCirculationSupplys(replyTo: ActorRef[CirculationSupplys]) extends Command
  final case class GetCirculationSupply(id:CirculationSupply.ID,ts0:Long,ts1:Long,replyTo: ActorRef[Option[CirculationSupply]]) extends Command
  final case class GetCirculationSupplyByToken(tid:String,ts0:Long,ts1:Long,replyTo: ActorRef[Option[CirculationSupply]]) extends Command
  final case class GetCirculationSupplyLast(sz:Int,tokens:Seq[String],replyTo: ActorRef[CirculationSupplys]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: CirculationSupplyStore = null //new CirculationSupplyStoreDB //new CirculationSupplyStoreCache

  def apply(store: CirculationSupplyStore = new CirculationSupplyStoreMem): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store)
  }

  private def registry(store: CirculationSupplyStore): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    Behaviors.receiveMessage {
      case GetCirculationSupplys(replyTo) =>
        replyTo ! CirculationSupplys(store.all)
        Behaviors.same

      case GetCirculationSupply(id,ts0,ts1,replyTo) =>
        replyTo ! store.?(id,ts0,ts1)
        Behaviors.same

      case GetCirculationSupplyByToken(tid,ts0,ts1,replyTo) =>
        replyTo ! store.findByToken(tid,ts0,ts1)
        Behaviors.same

      case GetCirculationSupplyLast(sz,tokens,replyTo) =>
        replyTo ! CirculationSupplys(store.last(sz,tokens))
        Behaviors.same      
    }
  }
}
