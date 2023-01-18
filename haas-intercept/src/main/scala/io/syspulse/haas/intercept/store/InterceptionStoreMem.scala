package io.syspulse.haas.intercept.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.haas.intercept._
import io.syspulse.haas.intercept.Interception.ID

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
    log.info(s"deleted: ${id}")
    if(sz == interceptions.size) Failure(new Exception(s"not found: ${id}")) else Success(this)  
  }

  def -(ix:Interception):Try[InterceptionStore] = {     
    del(ix.id)
  }

  def ?(id:ID):Option[Interception] = interceptions.get(id)

  def findByUser(uid:UUID):List[Interception] = {    
    interceptions.values.filter(i => {
        i.uid.isDefined && i.uid.get == uid
      }
    ).toList
  }

  def ??(txt:String):List[Interception] = {    
    interceptions.values.filter(v => {
        v.name.toLowerCase.matches(txt.toLowerCase) 
        //|| 
        //(v.contractAddress.isDefined && v.contractAddress.get.toLowerCase.matches(txt.toLowerCase))
      }
    ).toList
  }

  def search(txt:String):List[Interception] = ??(txt)

  def stop(id:Interception.ID):Option[Interception] = {
    val ix = interceptions.get(id) match {
      case Some(ix) => ix.status = "stopped"; Some(ix)
      case None => None
    }
    log.info(s"stop: ${ix}")
    ix
  }

  def start(id:Interception.ID):Option[Interception] = {
    val ix = interceptions.get(id) match {
      case Some(ix) => ix.status = "started"; Some(ix)
      case None => None
    }
    log.info(s"start: ${ix}")
    ix
  }
}
