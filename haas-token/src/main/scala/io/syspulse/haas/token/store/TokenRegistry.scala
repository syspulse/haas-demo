package io.syspulse.haas.token.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.haas.token.server._
import io.syspulse.haas.token._
import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID
import scala.util.Try

object TokenRegistry {
  val log = Logger(s"${this}")
  
  final case class GetTokens(replyTo: ActorRef[Tokens]) extends Command
  final case class GetTokensPage(from:Int,size:Int,replyTo: ActorRef[Tokens]) extends Command
  final case class GetToken(id:ID,replyTo: ActorRef[Try[Token]]) extends Command
  final case class SearchToken(txt:String,replyTo: ActorRef[Tokens]) extends Command
  final case class TypingToken(txt:String,replyTo: ActorRef[Tokens]) extends Command
  
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
        val tt = store.all
        replyTo ! Tokens(tt,Some(tt.size))
        Behaviors.same

      case GetTokensPage(from,size,replyTo) =>
        val tt = store.???(from,size)
        replyTo ! Tokens(tt,Some(tt.size))
        Behaviors.same

      case GetToken(id, replyTo) =>
        replyTo ! store.?(id)
        Behaviors.same

      case SearchToken(txt, replyTo) =>
        val tt = store.search(txt)
        replyTo ! Tokens(tt,Some(tt.size))
        Behaviors.same
      
      case TypingToken(txt, replyTo) =>
        val tt = store.typing(txt)
        replyTo ! Tokens(tt,Some(tt.size))
        Behaviors.same

      case CreateToken(tokenCreate, replyTo) =>
        val token = Token(tokenCreate.id, tokenCreate.symbol,tokenCreate.name,tokenCreate.contractAddress)
                
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
