package io.syspulse.haas.token.store

import scala.util.Try

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.skel.store.Store

import io.syspulse.haas.token.Config
import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

trait TokenStore extends Store[Token,ID] {
  
  def +(yell:Token):Try[TokenStore]
  def -(yell:Token):Try[TokenStore]
  def del(id:ID):Try[TokenStore]
  def ?(id:ID):Option[Token]
  def all:Seq[Token]
  def size:Long

  def ??(txt:String):List[Token]

  def connect(uri:String,index:String):TokenStore = this

  def scan(txt:String):List[Token]
  def search(txt:String):List[Token]
  def grep(txt:String):List[Token]
  def typing(txt:String):List[Token]
}
