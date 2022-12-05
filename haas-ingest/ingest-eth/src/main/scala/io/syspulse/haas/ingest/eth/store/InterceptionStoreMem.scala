package io.syspulse.haas.ingest.eth.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.haas.ingest.eth.intercept._
import io.syspulse.haas.ingest.eth.intercept.Interception.ID

class InterceptionStoreMem extends InterceptionStore {
  val log = Logger(s"${this}")
  
  var interceptions: Map[ID,Interception] = Map()

  def all:Seq[Interception] = interceptions.values.toSeq

  def size:Long = interceptions.size

  def +(ix:Interception):Try[InterceptionStore] = { 
    interceptions = interceptions + (ix.id -> ix)
    log.info(s"${ix}")
    Success(this)
  }

  def del(id:ID):Try[InterceptionStore] = { 
    val sz = interceptions.size
    interceptions = interceptions - id;
    log.info(s"${id}")
    if(sz == interceptions.size) Failure(new Exception(s"not found: ${id}")) else Success(this)  
  }

  def -(ix:Interception):Try[InterceptionStore] = {     
    del(ix.id)
  }

  def ?(id:ID):Option[Interception] = interceptions.get(id)

  def ??(txt:String):List[Interception] = {    
    interceptions.values.filter(v => {
        v.name.toLowerCase.matches(txt.toLowerCase) 
        //|| 
        //(v.contractAddress.isDefined && v.contractAddress.get.toLowerCase.matches(txt.toLowerCase))
      }
    ).toList
  }

  def scan(txt:String):List[Interception] = ??(txt)
  def search(txt:String):List[Interception] = ??(txt)
  def grep(txt:String):List[Interception] = ??(txt)
  def typing(txt:String):List[Interception] = ??(txt + ".*")
}
