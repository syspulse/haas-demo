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

  def +(ix:Interception):Try[Interception] = { 
    interceptions = interceptions + (ix.id -> ix)
    log.info(s"add: ${ix}")
    Success(ix)
  }

  def del(id:ID):Try[ID] = { 
    val sz = interceptions.size
    interceptions = interceptions - id;
    log.info(s"del: ${id}")
    if(sz == interceptions.size) Failure(new Exception(s"not found: ${id}")) else Success(id)  
  }

  def ?(id:ID):Try[Interception] = interceptions.get(id) match {
    case Some(ix) => Success(ix)
    case None => Failure(new Exception(s"not found: ${id}"))
  }

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
      case Some(ix) => ix.status = Interception.STOPPED; Some(ix)
      case None => None
    }
    log.info(s"stop: ${ix}")
    flush(ix)
    ix
  }

  def start(id:Interception.ID):Option[Interception] = {
    val ix = interceptions.get(id) match {
      case Some(ix) => ix.status = Interception.STARTED; Some(ix)
      case None => None
    }
    log.info(s"start: ${ix}")
    flush(ix)
    ix
  }

  def remember(ix:Interception,ia:InterceptionAlarm):Option[Interception] = {
    ix.remember(ia)
    val r = Some(ix)
    flush(r)
    r
  }

  def flush(ix:Option[Interception]):Try[InterceptionStore] = {
    Success(this)
  }
}
