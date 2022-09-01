package io.syspulse.haas.token.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

class TokenStoreMem extends TokenStore {
  val log = Logger(s"${this}")
  
  var Tokens: Map[ID,Token] = Map()

  def all:Seq[Token] = Tokens.values.toSeq

  def size:Long = Tokens.size

  def +(Token:Token):Try[TokenStore] = { 
    Tokens = Tokens + (Token.id -> Token)
    log.info(s"${Token}")
    Success(this)
  }

  def del(id:ID):Try[TokenStore] = { 
    val sz = Tokens.size
    Tokens = Tokens - id;
    log.info(s"${id}")
    if(sz == Tokens.size) Failure(new Exception(s"not found: ${id}")) else Success(this)  
  }

  def -(Token:Token):Try[TokenStore] = {     
    del(Token.id)
  }

  def ?(id:ID):Option[Token] = Tokens.get(id)

  def ??(txt:String):List[Token] = {
    Tokens.values.filter(v => 
      v.symbol.matches(txt) || 
      v.name.matches(txt) || 
      v.contractAddress.map(_.matches(txt)).isDefined
    ).toList
  }

  def scan(txt:String):List[Token] = ??(txt)
  def search(txt:String):List[Token] = ??(txt)
  def grep(txt:String):List[Token] = ??(txt)
  def typing(txt:String):List[Token] = ??(txt)
}
