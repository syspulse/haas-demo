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
    "have history == lag" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100, lag = 2)
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.size() should === (0)

      LastBlock.commit(1,"0x1")
      LastBlock.size() should === (1)
      LastBlock.commit(2,"0x2")
      LastBlock.size() should === (2)
      LastBlock.commit(3,"0x3")
      LastBlock.size() should === (3)
      LastBlock.commit(4,"0x4")
      LastBlock.size() should === (3)
      LastBlock.commit(5,"0x5")
      LastBlock.size() should === (3)
    }

    "not find reorg on start" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100)      
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      val r1 = LastBlock.isReorg(1,"0x111")

      r1 should === (List.empty)
    }

    "not find reorg on first block" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 0)      
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")

      val r1 = LastBlock.isReorg(1,"0x111")

      r1 should === (List.empty)
    }

    "not find reorg on duplicate block repeat" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 0)      
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")
      LastBlock.commit(1,"0x111")
      LastBlock.commit(1,"0x111")

      val r1 = LastBlock.isReorg(1,"0x111")

      r1 should === (List.empty)
    }

    "not find reorg on normal block sequence with lag == 0" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 0)      
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")      
      val r1 = LastBlock.isReorg(2,"0x222")
      r1 should === (List.empty)

      LastBlock.commit(2,"0x222")      
      val r2 = LastBlock.isReorg(3,"0x333")
      r2 should === (List.empty)
    }

    "not find reorg on normal block sequence with lag == 1" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 1) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")      
      val r1 = LastBlock.isReorg(2,"0x222")
      r1 should === (List.empty)

      LastBlock.commit(2,"0x222")      
      val r2 = LastBlock.isReorg(3,"0x333")
      r2 should === (List.empty)
    }

    "not find reorg on normal block sequence (10) with lag == 3" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 3) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      for(i <- 1 to 10) {
        LastBlock.commit(i,s"0x${i}${i}${i}")      
        val r = LastBlock.isReorg(i + 1,s"0x${i+1}${i+1}${i+1}")
        r should === (List.empty)
                
      }
    }
        
    "find reorg (deep=1) with lag == 2" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 2) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")      
      LastBlock.commit(2,"0x222")
      
      val r3 = LastBlock.isReorg(2,"0x222_2")
      r3 should === (List(Block(2,"0x222")))
    }

    "reorg (deep=1) with lag == 2" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 2) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")      
      LastBlock.commit(2,"0x222")
      
      val r3 = LastBlock.isReorg(2,"0x222_2")
      val r4 = LastBlock.reorg(r3)
      r3 should === (List(Block(2,"0x222")))
      r4 should === (List(Block(1,"0x111")))
    }

    "find reorg (deep=2) with lag == 2" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 2) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")      
      LastBlock.commit(2,"0x222")
      LastBlock.commit(3,"0x333")      
      
      val r3 = LastBlock.isReorg(2,"0x222_2")
      r3 should === (List(Block(3,"0x333"),Block(2,"0x222")))
    }

    "reorg (deep=2) with lag == 2" in {
      LastBlock.reset()
      
      LastBlock.set(1,1,100,lag = 2) 
      LastBlock.next() should === (1)
      LastBlock.current() should === (1)

      LastBlock.commit(1,"0x111")
      LastBlock.commit(2,"0x222")
      LastBlock.commit(3,"0x333")      
      
      val r3 = LastBlock.isReorg(2,"0x222_2")
      val r4 = LastBlock.reorg(r3)
      r3 should === (List(Block(3,"0x333"),Block(2,"0x222")))
      r4 should === (List(Block(1,"0x111")))

      LastBlock.commit(2,"0x222_2")
      LastBlock.next() should === (3)
      LastBlock.current() should === (2)
      LastBlock.last() should === (List(Block(2,"0x222_2"),Block(1,"0x111")))
    }
  }
  
}