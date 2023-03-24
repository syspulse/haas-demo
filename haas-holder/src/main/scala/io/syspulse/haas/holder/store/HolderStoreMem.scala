package io.syspulse.haas.holder.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.haas.core.Holders
import io.syspulse.haas.core.Holders.ID
import io.syspulse.haas.holder.server.Holderss
import scala.collection.mutable

class HolderStoreMem extends HolderStore {
  val log = Logger(s"${this}")
  
  var holders: Map[ID,mutable.SortedSet[Holders]] = Map()

  def all:Seq[Holders] = holders.values.flatten.toSeq

  def all(from:Option[Int],size:Option[Int]):Holderss = {
    var n = 0
    val hh = 
      holders.takeWhile{ case(token,h) => {
        val b = n < (from.getOrElse(0) + size.getOrElse(10))        
        n = n + h.size
        b        
      }}.values.flatten.toSeq
    
    Holderss(
      hh.drop(from.getOrElse(0)).take(size.getOrElse(10)),
      Some(holders.values.foldLeft(0L)((s,h) => s + h.size))
    )
  }

  def ???(id:ID,from:Option[Int],size:Option[Int]):Holderss = {
    holders.get(id) match {
      case Some(h) => 
        Holderss(h.drop(from.getOrElse(0)).take(size.getOrElse(10)).toSeq,total=Some(this.size))
      case None => 
        Holderss(Seq(),Some(0))
    }       
  }

  def size:Long = holders.size

  def +(h:Holders):Try[HolderStore] = { 
    holders = holders.get(h.token.toLowerCase) match {
      case Some(hh) => 
        hh.add(h)
        holders
      case None =>   
        holders + (h.token.toLowerCase -> mutable.SortedSet[Holders](h))
    }
    log.info(s"holders=${holders}")
    Success(this)
  }
  
  def ?(id:ID):Try[Holders] = holders.get(id) match {
    case Some(hh) => 
      val allHolders = hh.foldLeft(Map[String,BigInt]())((m,h) => m ++ h.addrs)
      Success(
        Holders(
          ts = 0L,
          token = id,
          addrs = allHolders
        )
      )     
    case None => Failure(new Exception(s"not found: ${id}"))
  }

  def ??(id:ID):Try[Seq[Holders]] = holders.get(id) match {
    case Some(hh) => 
      // val allHolders = hh.foldLeft(Map[String,BigInt]())((m,h) => m ++ h.addrs)
      // Success(
      //   Holders(
      //     ts = 0L,
      //     token = id,
      //     addrs = allHolders
      //   )
      // )
      Success(hh.toSeq)
    case None => Failure(new Exception(s"not found: ${id}"))
  }

  override def ??(ids:Seq[ID]):Seq[Holders] = {
    log.info(s"addr = ${ids}")
    ids.flatMap(addr => ??(addr.trim.toLowerCase).getOrElse(Seq()))
  }

}
