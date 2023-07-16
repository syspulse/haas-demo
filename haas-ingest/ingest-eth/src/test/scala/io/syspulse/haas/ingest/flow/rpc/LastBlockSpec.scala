package io.syspulse.haas.ingest.eth.flow.rpc

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.TryValues._
import scala.io.Source

import io.syspulse.skel.util.Util
import java.math.BigInteger

class LastBlockSpec extends AnyWordSpec with Matchers {
  //val testDir = this.getClass.getClassLoader.getResource(".").getPath + "../../../"

  "LastBlock" should {
    // "not find reorg on start" in {
    //   LastBlock.reset()
      
    //   LastBlock.set(1,1,100)      
    //   LastBlock.next() should === (1)
    //   LastBlock.current() should === (1)

    //   val r1 = LastBlock.isReorg(1,"0x111")

    //   r1 should === (None)
    // }

    // "not find reorg on first block" in {
    //   LastBlock.reset()
      
    //   LastBlock.set(1,1,100,lag = 0)      
    //   LastBlock.next() should === (1)
    //   LastBlock.current() should === (1)

    //   LastBlock.commit(1,"0x111")

    //   val r1 = LastBlock.isReorg(1,"0x111")

    //   r1 should === (None)
    // }

    // "not find reorg on duplicate block repeat" in {
    //   LastBlock.reset()
      
    //   LastBlock.set(1,1,100,lag = 0)      
    //   LastBlock.next() should === (1)
    //   LastBlock.current() should === (1)

    //   LastBlock.commit(1,"0x111")
    //   LastBlock.commit(1,"0x111")
    //   LastBlock.commit(1,"0x111")

    //   val r1 = LastBlock.isReorg(1,"0x111")

    //   r1 should === (None)
    // }

    "not find reorg on normal block sequence with lag == 0" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 0)      
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")      
      val r1 = LastBlock.isReorg(2,"0x222")
      r1 should === (None)

      LastBlock.commit(2,"0x222")      
      val r2 = LastBlock.isReorg(3,"0x333")
      r2 should === (None)
    }

    "not find reorg on normal block sequence with lag == 1" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 1) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")      
      val r1 = LastBlock.isReorg(2,"0x222")
      r1 should === (None)

      LastBlock.commit(2,"0x222")      
      val r2 = LastBlock.isReorg(3,"0x333")
      r2 should === (None)
    }

    "not find reorg on normal block sequence (10) with lag == 3" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 3) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      for(i <- 1 to 10) {
        LastBlock.commit(i,s"0x${i}${i}${i}")      
        val r = LastBlock.isReorg(i + 1,s"0x${i+1}${i+1}${i+1}")
        r should === (None)
                
      }
    }
    
    
    "find reorg (deep=1) with lag == 2" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 2) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")      
      LastBlock.commit(2,"0x222")
      
      val r3 = LastBlock.isReorg(1,"0x112")
      info(s"r3=${r3}")
      r3 should === (Some(Block(1,"0x111")))
    }
  }
  
}