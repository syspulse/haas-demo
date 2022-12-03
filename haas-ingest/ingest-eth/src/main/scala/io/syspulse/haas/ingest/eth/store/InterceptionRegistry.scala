package io.syspulse.haas.ingest.eth.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.haas.ingest.eth.server._
import io.syspulse.haas.ingest.eth.intercept._
import io.syspulse.haas.ingest.eth.intercept.Interception.ID

object InterceptionRegistry {
  val log = Logger(s"${this}")
  
  final case class GetInterceptions(replyTo: ActorRef[Interceptions]) extends Command
  final case class GetInterception(id:ID,replyTo: ActorRef[Option[Interception]]) extends Command
  final case class SearchInterception(txt:String,replyTo: ActorRef[Interceptions]) extends Command
  final case class TypingInterception(txt:String,replyTo: ActorRef[Interceptions]) extends Command
  
  final case class CreateInterception(interceptionCreate: InterceptionCreateReq, replyTo: ActorRef[Interception]) extends Command
  final case class RandomInterception(replyTo: ActorRef[Interception]) extends Command

  final case class DeleteInterception(id: ID, replyTo: ActorRef[InterceptionActionRes]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: InterceptionStore = null //new InterceptionStoreDB //new InterceptionStoreCache

  def apply(store: InterceptionStore = new InterceptionStoreMem): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store)
  }

  private def registry(store: InterceptionStore): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    Behaviors.receiveMessage {
      case GetInterceptions(replyTo) =>
        replyTo ! Interceptions(store.all)
        Behaviors.same

      case GetInterception(id, replyTo) =>
        replyTo ! store.?(id)
        Behaviors.same

      case SearchInterception(txt, replyTo) =>
        replyTo ! Interceptions(store.search(txt))
        Behaviors.same
      
      case TypingInterception(txt, replyTo) =>
        replyTo ! Interceptions(store.typing(txt))
        Behaviors.same

      case CreateInterception(c, replyTo) =>
        val interception = Interception(c.id, c.name, c.scriptId, c.alarm, c.uid)
                
        val store1 = store.+(interception)

        replyTo ! interception
        registry(store1.getOrElse(store))

      case RandomInterception(replyTo) =>
        
        //replyTo ! InterceptionRandomRes(secret,qrImage)
        Behaviors.same

      
      case DeleteInterception(vid, replyTo) =>
        val store1 = store.del(vid)
        replyTo ! InterceptionActionRes(s"Success",Some(vid.toString))
        registry(store1.getOrElse(store))
    }
  }
}
