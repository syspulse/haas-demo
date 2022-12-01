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
  
  var tokens: Map[ID,Token] = Map()

  def all:Seq[Token] = tokens.values.toSeq

  def size:Long = tokens.size

  def +(token:Token):Try[TokenStore] = { 
    tokens = tokens + (token.id -> token)
    log.info(s"${Token}")
    Success(this)
  }

  def del(id:ID):Try[TokenStore] = { 
    val sz = tokens.size
    tokens = tokens - id;
    log.info(s"${id}")
    if(sz == tokens.size) Failure(new Exception(s"not found: ${id}")) else Success(this)  
  }

  def -(token:Token):Try[TokenStore] = {     
    del(token.id)
  }

  def ?(id:ID):Option[Token] = tokens.get(id)

  def ??(txt:String):List[Token] = {    
    tokens.values.filter(v => {
        //log.info(s"'${txt}' :: ${v.symbol},${v.name}")
        v.id.toLowerCase.matches(txt.toLowerCase) || 
        v.symbol.toLowerCase.matches(txt.toLowerCase) ||
        v.name.toLowerCase.matches(txt.toLowerCase) || 
        (v.contractAddress.isDefined && v.contractAddress.get.toLowerCase.matches(txt.toLowerCase))
      }
    ).toList
  }

  def scan(txt:String):List[Token] = ??(txt)
  def search(txt:String):List[Token] = ??(txt)
  def grep(txt:String):List[Token] = ??(txt)
  def typing(txt:String):List[Token] = ??(txt + ".*")
}
