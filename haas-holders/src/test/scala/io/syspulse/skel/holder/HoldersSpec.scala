package io.syspulse.haas.holder

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.TryValues._
import scala.io.Source

import io.syspulse.haas.core._

import io.syspulse.skel.util.Util
import java.math.BigInteger
import java.nio.file.Path
import io.syspulse.haas.holder.store.HoldersStoreDir
import scala.util.Success

class HoldersSpec extends AnyWordSpec with Matchers {
  val testDir = this.getClass.getClassLoader.getResource(".").getPath + "../../../"

  "HoldersSpec" should {
    
    "path decoded: 'data/token/0x1f9840a85d5af5bf1d1762f925bdaddc4201f984/holders/2020/09/14/holders.csv'" in {
      val f = "data/token/0x1f9840a85d5af5bf1d1762f925bdaddc4201f984/holders/2020/09/14/holders.csv"      
      val r = HoldersStoreDir.parsePath(f)      
      r should === (Success(("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984",1600127999999L)))      
    }    

    "path decoded: '/s3/data/token/0x1ABCDEF/holders/1970/01/01/holders.csv'" in {
      val f = "/s3/data/token/0x1ABCDEF/holders/1970/01/01/holders.csv"
      val r = HoldersStoreDir.parsePath(f)      
      r should === (Success(("0x1abcdef",0L + 24 * 60 * 60 * 1000L - 1 )))
    }    
  }
}