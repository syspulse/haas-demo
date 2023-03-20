package io.syspulse.haas.token.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger
import scala.util.{Try,Success,Failure}

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.haas.token.server._
import io.syspulse.haas.token._
import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID
import io.syspulse.haas.core.TokenBlockchain

object TokenRegistry {
  val log = Logger(s"${this}")
  
  final case class GetTokens(replyTo: ActorRef[Tokens]) extends Command
  final case class GetTokensPage(from:Option[Int],size:Option[Int],replyTo: ActorRef[Tokens]) extends Command
  final case class GetToken(ids:Seq[ID],replyTo: ActorRef[Tokens]) extends Command
  final case class GetTokenByAddr(addrs:Seq[String],replyTo: ActorRef[Tokens]) extends Command
  final case class SearchToken(txt:String,from:Option[Int],size:Option[Int],replyTo: ActorRef[Tokens]) extends Command
  final case class TypingToken(txt:String,from:Option[Int],size:Option[Int],replyTo: ActorRef[Tokens]) extends Command
  
  final case class CreateToken(req: TokenCreateReq, replyTo: ActorRef[Token]) extends Command
  final case class UpdateToken(id:ID, req: TokenUpdateReq, replyTo: ActorRef[Try[Token]]) extends Command
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
        replyTo ! tt
        Behaviors.same

      case GetToken(ids, replyTo) =>
        val tt = store.??(ids)
        replyTo ! Tokens(tt,Some(tt.size))
        Behaviors.same

      case GetTokenByAddr(addrs, replyTo) =>
        val t = store.search(addrs,Some(0),Some(addrs.size))
        // replyTo ! (t.tokens match {
        //   case Seq(t) => Success(t)
        //   case _ => Failure(new Exception(s"not found: ${addr}"))
        // })
        replyTo ! t
        Behaviors.same

      case SearchToken(txt, from,size, replyTo) =>
        val tt = store.search(txt,from,size)
        replyTo ! tt
        Behaviors.same
      
      case TypingToken(txt, from,size,replyTo) =>
        val tt = store.typing(txt,from,size)
        replyTo ! tt
        Behaviors.same

      case CreateToken(req, replyTo) =>
        val token = Token(
          req.id, 
          req.symbol,
          req.name,
          cat = req.cat.getOrElse(Seq()).toList,
          icon = req.icon,
          dcml = req.decimals,
          chain = req.contracts.getOrElse(Map()).map{ case(bid,addr) => 
            TokenBlockchain(bid.toLowerCase,addr.toLowerCase)
          }.toSeq)
                
        val store1 = store.+(token)

        replyTo ! token
        Behaviors.same
        
      case UpdateToken(id, req, replyTo) =>
        val t1 = store.update(id,req.symbol,req.name,req.cat,req.icon,req.decimals,
          req.contracts.map { 
            _.map{ case(bid,addr) => 
              TokenBlockchain(bid.toLowerCase,addr.toLowerCase)
            }.toSeq
          }
        )

        replyTo ! t1
        Behaviors.same

      case RandomToken(replyTo) =>
        //replyTo ! TokenRandomRes(secret,qrImage)
        Behaviors.same
      
      case DeleteToken(id, replyTo) =>
        val store1 = store.del(id)
        replyTo ! TokenActionRes(s"deleted",Some(id.toString))
        Behaviors.same
    }
  }
}
