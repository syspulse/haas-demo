package io.syspulse.haas.token.store

import scala.util.Try

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.skel.store.Store

import io.syspulse.haas.token.Config
import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID
import io.syspulse.haas.token.server.Tokens
import io.syspulse.haas.core.TokenBlockchain

trait TokenStore extends Store[Token,ID] {
  def getKey(t:Token):ID = t.id
  
  def +(yell:Token):Try[TokenStore]
  
  def del(id:ID):Try[TokenStore]
  
  def all:Seq[Token]
  def size:Long

  def search(txt:Seq[String],from:Option[Int],size:Option[Int]):Tokens
  def search(txt:String,from:Option[Int],size:Option[Int]):Tokens

  def ???(from:Option[Int],size:Option[Int]):Tokens

  def scan(txt:String,from:Option[Int],size:Option[Int]):Tokens
  
  def grep(txt:String,from:Option[Int],size:Option[Int]):Tokens
  def typing(txt:String,from:Option[Int],size:Option[Int]):Tokens

  def update(id:ID, symbol:Option[String] = None, name:Option[String] = None,
             cat:Option[List[String]] = None, icon:Option[String] = None, dcml:Option[Int] = None,
             contracts:Option[Seq[TokenBlockchain]] = None):Try[Token]

  protected def modify(t0:Token, symbol:Option[String] = None, name:Option[String] = None,
             cat:Option[List[String]] = None, icon:Option[String] = None, dcml:Option[Int] = None,
             chain:Option[Seq[TokenBlockchain]] = None):Token = {
    (for {
      t1 <- Some(if(symbol.isDefined) t0.copy(symbol = symbol.get) else t0)
      t2 <- Some(if(name.isDefined) t1.copy(name = name.get) else t1)
      t3 <- Some(if(cat.isDefined) t2.copy(cat = cat.get) else t2)
      t4 <- Some(if(icon.isDefined) t3.copy(icon = icon) else t3)
      t5 <- Some(if(dcml.isDefined) t4.copy(dcml = dcml) else t4)
      t6 <- Some(if(chain.isDefined) t5.copy(chain = chain.get) else t5)
    } yield t6).get    
  }
}
