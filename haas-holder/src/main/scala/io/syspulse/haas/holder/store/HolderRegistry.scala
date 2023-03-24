package io.syspulse.haas.holder.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger
import scala.util.{Try,Success,Failure}

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.haas.holder.server._
import io.syspulse.haas.holder._
import io.syspulse.haas.core.Holders
import io.syspulse.haas.core.Holders.ID

object HolderRegistry {
  val log = Logger(s"${this}")
  
  final case class GetHolderss(replyTo: ActorRef[Holderss]) extends Command
  final case class GetHoldersPage(id:ID,ts0:Option[Long],ts1:Option[Long],from:Option[Int],size:Option[Int],limit:Option[Int],replyTo: ActorRef[Holderss]) extends Command
  final case class GetHolders(ids:Seq[ID],replyTo: ActorRef[Holderss]) extends Command
    
  // this var reference is unfortunately needed for Metrics access
  var store: HolderStore = null //new HolderStoreDB //new HolderStoreCache

  def apply(store: HolderStore = new HolderStoreMem): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store)
  }

  private def registry(store: HolderStore): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    Behaviors.receiveMessage {
      case GetHolderss(replyTo) =>
        val tt = store.all
        replyTo ! Holderss(tt,Some(tt.size))
        Behaviors.same

      case GetHoldersPage(id,ts0,ts1,from,size,limit,replyTo) =>
        val tt = store.???(id,ts0,ts1,from,size,limit)
        replyTo ! tt
        Behaviors.same

      case GetHolders(ids, replyTo) =>
        val tt = store.??(ids)
        replyTo ! Holderss(tt,Some(tt.size))
        Behaviors.same
    }
  
  }
}
