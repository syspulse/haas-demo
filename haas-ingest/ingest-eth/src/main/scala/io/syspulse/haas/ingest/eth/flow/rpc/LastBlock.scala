package io.syspulse.haas.ingest.eth.flow.rpc

import java.util.concurrent.atomic.AtomicLong
import io.syspulse.skel.ingest.flow.Flows

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

case class LastBlock(block:Long, blockStart:Long, blockEnd:Long, stateStore:Option[String])

object LastBlock {
  private val log = Logger(s"LastBlock")

  @volatile
  private var lastBlock: Option[LastBlock] = None

  override def toString() = s"${lastBlock}"

  def commit(block:Long) = {
    lastBlock.synchronized {
      log.info(s"COMMIT: ${block}")
      lastBlock = lastBlock.map(lb => lb.copy(block = block + 1))
    }
  }

  def isDefined = lastBlock.isDefined

  def set(block:Long,blockStart:Long,blockEnd:Long,stateFile:Option[String]) = {
    lastBlock = lastBlock.synchronized {
      lastBlock match {
        case Some(_) => lastBlock
        case None => Some(LastBlock(block,blockStart,blockEnd,stateFile))    
      }
    }    
  }

  def current() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => lb.block
      case None => -1
    }
  }

  def last() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => lb.blockEnd
      case None => -1
    }
  }
}

