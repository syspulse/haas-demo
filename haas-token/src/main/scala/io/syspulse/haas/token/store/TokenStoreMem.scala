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
import io.syspulse.haas.token.server.Tokens

class TokenStoreMem extends TokenStore {
  val log = Logger(s"${this}")
  
  var tokens: Map[ID,Token] = Map()

  def all:Seq[Token] = tokens.values.toSeq

  def ???(from:Option[Int],size:Option[Int]):Tokens = 
    Tokens(all.drop(from.getOrElse(0)).take(size.getOrElse(10)),total=Some(this.size))

  def size:Long = tokens.size

  def +(t:Token):Try[TokenStore] = { 
    tokens = tokens + (t.id -> t)
    log.info(s"${t}")
    Success(this)
  }

  def del(id:ID):Try[TokenStore] = { 
    val sz = tokens.size
    tokens = tokens - id;
    log.info(s"${id}")
    if(sz == tokens.size) Failure(new Exception(s"not found: ${id}")) else Success(this)  
  }

  // def -(token:Token):Try[TokenStore] = {     
  //   del(token.id)
  // }

  def ?(id:ID):Try[Token] = tokens.get(id) match {
    case Some(t) => Success(t)
    case None => Failure(new Exception(s"not found: ${id}"))
  }

  def ??(txt:String,from:Option[Int],size:Option[Int]):Tokens = {    
    val tt = tokens.values.filter(v => {
        //log.info(s"'${txt}' :: ${v.symbol},${v.name}")
        v.id.toLowerCase.matches(txt.toLowerCase) || 
        v.symbol.toLowerCase.matches(txt.toLowerCase) ||
        v.name.toLowerCase.matches(txt.toLowerCase) || 
        (v.addr.isDefined && v.addr.get.toLowerCase.matches(txt.toLowerCase))
      }
    ).drop(from.getOrElse(0)).take(size.getOrElse(10)).toList

    Tokens(tt,Some(this.size))
  }

  def scan(txt:String,from:Option[Int],size:Option[Int]):Tokens = ??(txt,from,size)
  def search(txt:String,from:Option[Int],size:Option[Int]):Tokens = ??(txt,from,size)
  def grep(txt:String,from:Option[Int],size:Option[Int]):Tokens = ??(txt,from,size)
  def typing(txt:String,from:Option[Int],size:Option[Int]):Tokens = ??(txt + ".*",from,size)
}
