package io.syspulse.haas.ingest.eth.flow.rpc

import java.util.concurrent.atomic.AtomicLong
import io.syspulse.skel.ingest.flow.Flows

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

case class Block(num:Long,hash:String)

case class LastBlock(
  next:Long,       // next EXPECTED block !
  blockStart:Long, 
  blockEnd:Long, 
  stateStore:Option[String], 
  lag:Int = 0, 
  last:List[Block] = List(), // hashes of last blocks to detect reorg
  blockReorg:Option[Long] = None
)

object LastBlock {
  private val log = Logger(s"LastBlock")

  @volatile
  private var lastBlock: Option[LastBlock] = None

  override def toString() = s"${lastBlock}"

  // expects STRICTLY sequential blocks !
  def isReorg(block:Long,blockHash:String):List[Block] = {
    val reorgs = lastBlock.synchronized {      
      lastBlock match {
        case Some(lb) =>
          
          // println(s"============> ${block}: ${lastBlock}")
          if(lb.last.size == 0)
            return List.empty

          // check for the same block repeated: if current==last and hashes are the same it is not reorg
          if(lb.next-1 == block && lb.last.head.hash == blockHash) 
            return List.empty

          if(block > lb.next) {
            log.error(s"Lost blocks: next=${lb.next}, new=${block}: Reduce RCP query interval")
            return List.empty
          }

          // if next block, no re-org
          if(block == lb.next)
            return List.empty
                    
          // find reorg-ed block
          val blockIndex = lb.last.zipWithIndex.find{ case(b,i) =>
            b.num == block && b.hash != blockHash            
          }
          val reorgs = blockIndex match {
            case Some(bi) => lb.last.take(bi._2 + 1)
            case None => List()
          }
          
          log.info(s"reorg block: next=${lb.next}, new=${block}: reorgs=${reorgs}")
          reorgs
          
        case None => 
          List.empty
      }
    }

    reorgs
  }

  def reorg(blocks:List[Block]):List[Block] = {
    lastBlock.synchronized {      
      lastBlock match {
        case Some(lb) =>
          // infrequent operation, so safe to "toSet"
          println(s"last=${lb.last}: blocks=${blocks}")
          lastBlock = Some(lb.copy(last = lb.last.toSet.&~(blocks.toSet).toList))
          lastBlock.get.last
        case None => 
          List.empty
      }
    }    
  }

  def commit(block:Long,blockHash:String) = {
    lastBlock.synchronized {
      log.info(s"COMMIT: (${block},${blockHash})")
      lastBlock = lastBlock.map(lb => {        
        val last = 
          if(lb.last.size > lb.lag)
            lb.last.take(lb.lag)
          else
            lb.last

        lb.copy(next = block + 1, last = last.+:(Block(block,blockHash)))
      })
    }
  }

  def isDefined = lastBlock.isDefined

  def set(next:Long,blockStart:Long,blockEnd:Long = Long.MaxValue,stateFile:Option[String] = None, lag:Int = 0) = {
    lastBlock = lastBlock.synchronized {
      lastBlock match {
        case Some(_) => lastBlock
        case None => Some(LastBlock(next,blockStart,blockEnd,stateFile, lag = lag))    
      }
    }    
  }

  def next() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => lb.next
      case None => -1
    }
  }

  def current() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => if(lb.next == lb.blockStart) lb.next else lb.next - 1
      case None => -1
    }
  }

  def end() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => lb.blockEnd
      case None => -1
    }
  }

  // size can be lag + 1 maximum
  def size() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => lb.last.size
      case None => 0
    }
  }

  def last() = lastBlock.synchronized {
    lastBlock match {
      case Some(lb) => lb.last
      case None => List.empty
    }
  }

  def reset() = lastBlock = None
}

