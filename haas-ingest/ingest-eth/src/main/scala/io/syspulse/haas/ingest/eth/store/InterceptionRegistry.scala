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
import io.syspulse.haas.ingest.eth.script._
import io.syspulse.haas.ingest.eth.intercept.Interception.ID
import io.syspulse.skel.util.Util

import io.syspulse.haas.ingest.eth.intercept.Interceptor

object InterceptionRegistry {
  val log = Logger(s"${this}")
  
  final case class GetInterceptions(replyTo: ActorRef[Interceptions]) extends Command
  final case class GetInterception(id:ID,replyTo: ActorRef[Option[Interception]]) extends Command
  final case class SearchInterception(txt:String,replyTo: ActorRef[Interceptions]) extends Command
  final case class TypingInterception(txt:String,replyTo: ActorRef[Interceptions]) extends Command
  
  final case class CreateInterception(interceptionCreate: InterceptionCreateReq, replyTo: ActorRef[Interception]) extends Command
  final case class CommandInterception(interceptionComman: InterceptionCommandReq, replyTo: ActorRef[InterceptionActionRes]) extends Command

  final case class DeleteInterception(id: ID, replyTo: ActorRef[InterceptionActionRes]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: InterceptionStore = null //new InterceptionStoreDB //new InterceptionStoreCache

  def apply(store: InterceptionStore,storeScript:ScriptStore, interceptor:Interceptor[_]): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store,storeScript,interceptor)
  }

  private def registry(store: InterceptionStore,storeScript:ScriptStore,interceptor:Interceptor[_]): Behavior[io.syspulse.skel.Command] = {
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
        // 1 = 1 association for user script

        val src = 
          if(c.script.trim.startsWith("id://")) {
            val id = c.script.trim.stripPrefix("id://")
            val s = storeScript.?(id)
            if(s.isDefined)
              s.get.src
            else
              ""
          } else {
            c.script
          }
        
        val scriptId = Util.sha256(c.script)
        val script = Script(scriptId,"js",src,c.name)
        storeScript.+(script)
        
        val ix = Interception(c.id.getOrElse(UUID.random), c.name, script.id, c.alarm, c.uid)
        
        val store1 = store.+(ix)

        interceptor.+(ix)

        replyTo ! ix
        registry(store1.getOrElse(store),storeScript,interceptor)

      case CommandInterception(c, replyTo) =>
        
        val st = c.command match {
          case "start" => interceptor.start(c.id.get); "started"
          case "stop" => interceptor.stop(c.id.get); "stopped"
          case _ => "unknown"
        }
        
        replyTo ! InterceptionActionRes(st,Some(c.id.toString))
        Behaviors.same
      
      case DeleteInterception(id, replyTo) =>
        val store1 = store.del(id)

        interceptor.-(id)

        replyTo ! InterceptionActionRes(s"Success",Some(id.toString))
        registry(store1.getOrElse(store),storeScript,interceptor)
    }
  }
}