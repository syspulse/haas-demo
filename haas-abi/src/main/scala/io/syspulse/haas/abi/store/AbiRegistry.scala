package io.syspulse.haas.abi.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.skel.crypto.eth.abi._

import io.syspulse.haas.abi.server._
import io.syspulse.haas.abi._

import scala.util.Try

object AbiRegistry {
  val log = Logger(s"${this}")
  
  final case class GetAbis(entity:String,from:Option[Int],size:Option[Int],replyTo: ActorRef[Abis]) extends Command
  final case class GetAbi(entity:String,id:String,replyTo: ActorRef[Try[Abi]]) extends Command
  final case class GetSearchAbi(entity:String,txt:String,from:Option[Int],size:Option[Int],replyTo: ActorRef[Abis]) extends Command
  final case class GetTypingAbi(txt:String,from:Option[Int],size:Option[Int],replyTo: ActorRef[Abis]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: AbiStore = null //new AbiStoreDB //new AbiStoreCache

  def apply(store: AbiStore): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store)
  }

  private def registry(store: AbiStore): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    Behaviors.receiveMessage {
      case GetAbis(entity,from,size,replyTo) =>
        val aa = entity.trim match {
          case AbiStore.CONTRACT => 
            val cc = store.all(from,size)
            Abis(cc._1.map(c => Abi(addr = Some(c.addr),json = Some(c.json))),Some(cc._2))
          case AbiStore.EVENT => 
            val ee = store.events.all(from,size)
            Abis(ee._1.map(e => Abi(hex = Some(e.hex),tex = Some(e.tex))),Some(ee._2))
          case AbiStore.FUNCTION => 
            val ff = store.functions.all(from,size)
            Abis(ff._1.map(f => Abi(hex = Some(f.hex),tex = Some(f.tex))),Some(ff._2))
          case _ =>
            val cc = store.all(from,size)
            val ee = store.events.all(from,size)
            val ff = store.functions.all(from,size)
            Abis( abis =
              cc._1.map(c => Abi(ent = Some(AbiStore.CONTRACT),addr = Some(c.addr),json = Some(c.json))) ++ 
              ee._1.map(e => Abi(ent = Some(AbiStore.EVENT),hex = Some(e.hex),tex = Some(e.tex))) ++
              ff._1.map(f => Abi(ent = Some(AbiStore.FUNCTION),hex = Some(f.hex),tex = Some(f.tex))),

              total = Some(cc._2 + ee._2 + ff._2)
            )
        }
        replyTo ! aa
        Behaviors.same

      case GetAbi(entity,id,replyTo) =>
        val a = entity.trim match {
          case AbiStore.CONTRACT => 
            val c = store.?(id)
            c.map(c => Abi(addr = Some(c.addr),json = Some(c.json)))
          case AbiStore.EVENT => 
            val e = store.events.?((id,0))
            e.map(e => Abi(hex = Some(e.hex),tex = Some(e.tex)))
          case AbiStore.FUNCTION => 
            val f = store.functions.?((id,0))
            f.map(f => Abi(hex = Some(f.hex),tex = Some(f.tex)))
        }
        replyTo ! a
        Behaviors.same
                
      case GetSearchAbi(entity,txt,from,size,replyTo) =>
        val aa = entity.trim match {
          case AbiStore.CONTRACT => 
            val cc = store.search(txt,from,size)
            Abis(cc._1.map(c => Abi(addr = Some(c.addr),json = Some(c.json))),Some(cc._2))
          case AbiStore.EVENT => 
            val ee = store.events.search(txt,from,size)
            Abis(ee._1.map(e => Abi(hex = Some(e.hex),tex = Some(e.tex))),Some(ee._2))
          case AbiStore.FUNCTION => 
            val ff = store.functions.search(txt,from,size)
            Abis(ff._1.map(f => Abi(hex = Some(f.hex),tex = Some(f.tex))),Some(ff._2))

          case _ =>
            val cc = store.search(txt,from,size)
            val ee = store.events.search(txt,from,size)
            val ff = store.functions.search(txt,from,size)
            Abis( abis =
              cc._1.map(c => Abi(ent = Some(AbiStore.CONTRACT),addr = Some(c.addr),json = Some(c.json))) ++ 
              ee._1.map(e => Abi(ent = Some(AbiStore.EVENT),hex = Some(e.hex),tex = Some(e.tex))) ++
              ff._1.map(f => Abi(ent = Some(AbiStore.FUNCTION),hex = Some(f.hex),tex = Some(f.tex))),

              total = Some(cc._2 + ee._2 + ff._2)
            )
        }
        replyTo ! aa
        Behaviors.same        

      // case GetTypingAbi(txt,from,size,replyTo) =>
      //   replyTo ! store.typing(txt,from,size)
      //   Behaviors.same
      
    }
  }
}
