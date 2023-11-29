package io.syspulse.haas.ingest.eth.flow.rpc3

import java.util.concurrent.atomic.AtomicLong
import io.syspulse.skel.ingest.flow.Flows

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

case class Block(num:Long,hash:String,ts:Long = 0L,txCound:Long = 0)

class CursorBlock(file:String = "LAST_BLOCK") {
  private val log = Logger(this.getClass)

  override def toString() = s"${current} [${blockStart} : ${blockEnd}]"

  var current:Long = 0
  var blockStart:Long = 0
  var blockEnd:Long = 0
  
  val lag:Int = 0            // lag means how many blocks wait until moving to the next block
                             // this is also a depth of last history to check for reorg 
    
  var lagging:Int = 0        // current lag counter (from lag to 0). On lagging == 0 next is incremented

  def init(blockStart:Long,blockEnd:Long) = {
    this.synchronized {
      this.current = blockStart
      this.blockStart = blockStart
      this.blockEnd = blockEnd
    }    
  }

  def set(current:Long) = this.synchronized {
    this.current = current    
  }

  def get() = this.synchronized {
    this.current
  }

  def next() = this.synchronized {
    current + 1
  }

  def commit(block:Long) = this.synchronized {
    current = block + 1    
  }

}

