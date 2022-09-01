package io.syspulse.haas.token.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID
import io.syspulse.haas.token._

object TokenRegistry {
  val log = Logger(s"${this}")
  
  final case class GetTokens(replyTo: ActorRef[Tokens]) extends Command
  final case class GetToken(id:ID,replyTo: ActorRef[Option[Token]]) extends Command
  final case class SearchToken(txt:String,replyTo: ActorRef[List[Token]]) extends Command
  
  final case class CreateToken(tokenCreate: TokenCreateReq, replyTo: ActorRef[Token]) extends Command
  final case class RandomToken(replyTo: ActorRef[Token]) extends Command

  final case class DeleteToken(id: ID, replyTo: ActorRef[TokenActionRes]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: TokenStore = null //new TokenStoreDB //new TokenStoreCache

  def apply(store: TokenStore = new TokenStoreMem): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store)
  }

  private def registry(store: TokenStore): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    Behaviors.receiveMessage {
      case GetTokens(replyTo) =>
        replyTo ! Tokens(store.all)
        Behaviors.same

      case GetToken(id, replyTo) =>
        replyTo ! store.?(id)
        Behaviors.same

      case SearchToken(txt, replyTo) =>
        replyTo ! store.??(txt)
        Behaviors.same


      case CreateToken(tokenCreate, replyTo) =>
        val token = Token(tokenCreate.id, tokenCreate.symbol,tokenCreate.name,tokenCreate.address)
                
        val store1 = store.+(token)

        replyTo ! token
        registry(store1.getOrElse(store))

      case RandomToken(replyTo) =>
        
        //replyTo ! TokenRandomRes(secret,qrImage)
        Behaviors.same

      
      case DeleteToken(vid, replyTo) =>
        val store1 = store.del(vid)
        replyTo ! TokenActionRes(s"Success",Some(vid.toString))
        registry(store1.getOrElse(store))
    }
  }
}
