package io.syspulse.haas.token.store

import scala.util.Try

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.skel.store.Store

import io.syspulse.haas.token.Config
import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID
import io.syspulse.haas.token.server.Tokens

trait TokenStore extends Store[Token,ID] {
  def getKey(t:Token):ID = t.id
  
  def +(yell:Token):Try[TokenStore]
  
  def del(id:ID):Try[TokenStore]
  
  def all:Seq[Token]
  def size:Long

  def search(txt:Seq[String],from:Option[Int],size:Option[Int]):Tokens
  def search(txt:String,from:Option[Int],size:Option[Int]):Tokens

  def ???(from:Option[Int],size:Option[Int]):Tokens

  def connect(uri:String,index:String):TokenStore = this

  def scan(txt:String,from:Option[Int],size:Option[Int]):Tokens
  
  def grep(txt:String,from:Option[Int],size:Option[Int]):Tokens
  def typing(txt:String,from:Option[Int],size:Option[Int]):Tokens
}
