package io.syspulse.haas.supply.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.haas.supply.server._
import io.syspulse.haas.supply.Supply

object SupplyRegistry {
  val log = Logger(s"${this}")
  
  final case class GetSupplys(replyTo: ActorRef[Supplys]) extends Command
  final case class GetSupply(id:Supply.ID,ts0:Long,ts1:Long,replyTo: ActorRef[Option[Supply]]) extends Command
  final case class GetSupplyByToken(tid:String,ts0:Long,ts1:Long,replyTo: ActorRef[Option[Supply]]) extends Command
  final case class GetSupplyLast(tokens:Seq[String],from:Int,size:Int,replyTo: ActorRef[Supplys]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: SupplyStore = null //new SupplyStoreDB //new SupplyStoreCache

  def apply(store: SupplyStore = new SupplyStoreMem): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store)
  }

  private def registry(store: SupplyStore): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    Behaviors.receiveMessage {
      case GetSupplys(replyTo) =>
        replyTo ! Supplys(store.all)
        Behaviors.same

      case GetSupply(id,ts0,ts1,replyTo) =>
        replyTo ! store.?(id,ts0,ts1)
        Behaviors.same

      case GetSupplyByToken(tid,ts0,ts1,replyTo) =>
        replyTo ! store.findByToken(tid,ts0,ts1)
        Behaviors.same

      case GetSupplyLast(tokens,from,size,replyTo) =>
        replyTo ! Supplys(store.lastByTokens(tokens,from,size))
        Behaviors.same      
    }
  }
}
