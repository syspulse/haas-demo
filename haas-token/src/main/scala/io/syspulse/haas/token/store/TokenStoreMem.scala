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
import io.syspulse.haas.core.TokenBlockchain
import io.syspulse.haas.core.TokenLocks

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

  def ?(id:ID):Try[Token] = tokens.get(id) match {
    case Some(t) => Success(t)
    case None => Failure(new Exception(s"not found: ${id}"))
  }

  override def ??(ids:Seq[ID]):Seq[Token] = {
    log.info(s"ids = ${ids}")
    ids.flatMap(id => tokens.get(id.trim))
  }

  def search(txt:Seq[String],from:Option[Int],size:Option[Int]):Tokens = {    
    val tt = tokens.values.filter(v => {
        txt.filter( txt => 
          //txt.trim.size >= 3 && 
          (
            v.id.toLowerCase.matches(txt.toLowerCase) || 
            v.symbol.toLowerCase.matches(txt.toLowerCase) ||
            v.name.toLowerCase.matches(txt.toLowerCase) || 
            (v.addr.isDefined && v.addr.get.toLowerCase.matches(txt.toLowerCase))
          )
        ).size > 0        
      }
    )
    val tt2 = tt.drop(from.getOrElse(0)).take(size.getOrElse(10)).toList

    Tokens(tt2,Some(tt.size))
  }

  def scan(txt:String,from:Option[Int],size:Option[Int]):Tokens = search(txt.split(",").toSeq,from,size)
  def search(txt:String,from:Option[Int],size:Option[Int]):Tokens = 
    search(txt.split(",").toSeq.flatMap(txt => if(txt.trim.size >=3) Some(s".*${txt}.*") else None),from,size)

  def grep(txt:String,from:Option[Int],size:Option[Int]):Tokens = search(txt.split(",").toSeq,from,size)
  def typing(txt:String,from:Option[Int],size:Option[Int]):Tokens = 
    if(txt.size <3 )
      Tokens(Seq(),Some(0))
    else
      search(Seq(txt + ".*"),from,size)

  def update(id:ID, symbol:Option[String] = None, name:Option[String] = None, addr: Option[String] = None,
             cat:Option[List[String]] = None, icon:Option[String] = None, dcml:Option[Int] = None,
             contracts:Option[Seq[TokenBlockchain]] = None,
             locks:Option[Seq[TokenLocks]] = None):Try[Token] = 
    this.?(id) match {
      case Success(t) => 
        val t1 = modify(t,symbol,name,addr,cat,icon,dcml,contracts,locks)
        this.+(t1)
        Success(t1)
      case f => f
    }
}
