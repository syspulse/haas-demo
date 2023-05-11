package io.syspulse.haas.supply.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger
import io.jvm.uuid._
import io.syspulse.skel.util.Util

import io.syspulse.haas.supply._

import spray.json._
import DefaultJsonProtocol._

import os._
import scala.collection.SortedSet

import io.syspulse.haas.supply.store
import io.syspulse.haas.supply

object SupplyStoreDir {
  val FILE_NAME = "supply.json"
}

// temporary structure to catch tokenId
case class TokenCirculating(tid:String,sd:SupplyData)

// Preload from file during start
class SupplyStoreDir(dir:String = "store/",preload:Boolean = true) extends SupplyStoreMem {
  import CirculatingSupplyJson._
  
  if(preload) {
    val ss = load(dir)
    ss.foreach(s => this.+(s))
  }
  
  def load(dir:String):Seq[Supply] = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    val supplys:Seq[TokenCirculating] = os.walk(storeDir)
      .filter(_.toIO.isFile())
      .filter( f => {
        log.debug(s"${f}: ${f.toIO.getName}")
        f.toIO.getName == SupplyStoreDir.FILE_NAME
      })
      .map(f => {
        log.info(s"Loading file: ${f}")
        (f,os.read(f))
      })
      .flatMap{ case (f,data) => 
        data.split("\n").map(d => (f,d))        
      }
      .flatMap{ case (f,data) => {
        parseFileData(data).map{ case(tid,c) => TokenCirculating(tid,c)}
      }}
      
    log.debug(s"${supplys}")
    log.info(s"Loaded: ${supplys.size}: ${supplys.map(_.tid).mkString(",")}")

    val ss = supplys.groupBy(_.tid).map{ case(tid,circ) => {
      Supply(
        id = UUID.random,
        name = tid, // no name specified at this stage
        tokenId = tid,

        history = SortedSet.from[SupplyData](circ.map(_.sd))
      )
    }}.toSeq
    
    log.info(s"Supplys: ${ss.size}")
    ss
  }

  def parseFileData(data:String):Option[(String,SupplyData)] = {
    try {
      val cs = data.parseJson.convertTo[CirculatingSupply]
      
      val s = SupplyData(
        ts = cs.ts,
        totalSupply = TotalSupply(cs.totalSupply.total,cs.totalSupply.dstr.map{case(b,v) => BlockchainValues(b,v)}.toSeq),
        circSupply = CircSupply(cs.circSupply.total,cs.circSupply.dstr.map{case(b,v) => BlockchainValues(b,v)}.toSeq),
        
        inflation = 0.0, // not calculated any longer
        price = cs.price,
        marketCap = cs.price * cs.circSupply.total.toDouble,
        
        categories = cs.categories.map{ case(cat,cc) => Category(cat,cc.total,cc.dstr.map{case(b,v)=>BlockchainValues(b,v)}.toSeq)}.toSeq,
        locks = cs.locks.map{ case(b,ll) => Lock(b,ll.map{case(a,li)=>LockInfo(a,li.tag,li.value)}.toSeq)}.toSeq,
        
        holders = Holders(cs.holders.total,cs.holders.dstr.map{case(b,v) => HolderBlockchainValues(b,v)}.toSeq),
        holdersGrowth = Holders(cs.holdersGrowth.total,cs.holdersGrowth.dstr.map{case(b,v) => HolderBlockchainValues(b,v)}.toSeq),
        uniqueHoldersUp = Holders(cs.uniqueHoldersUp.total,cs.uniqueHoldersUp.dstr.map{case(b,v) => HolderBlockchainValues(b,v)}.toSeq),
        uniqueHoldersDown = Holders(cs.uniqueHoldersDown.total,cs.uniqueHoldersDown.dstr.map{case(b,v) => HolderBlockchainValues(b,v)}.toSeq),

        topHolders = cs.topHolders.map(th => TopHolders(th.addr,th.r,TV(th.tv.total,th.tv.dstr.map{case(b,v) => BlockchainValues(b,v)}.toSeq)))
      )

      Some((cs.token_id,s))
    } catch {
      case e:Exception => log.error(s"could not parse data: ${data}",e); None
    } 
  }

  def tsToDir(ts:Long) = Util.nextTimestampDir("{yyyy}/{MM}/{dd}/",ts)

  def getDayRange(ts0:Long,ts1:Long) = {
    val DAY = 1000L * 60L * 60L * 24L
    val days = ((ts1 - ts0) / DAY).toInt
    for( d <- Range(0,days)) {
      val ts = ts0 + d
      val dir = tsToDir(ts)
      dir
    }
  }

  override def ?(id:Supply.ID,ts0:Long,ts1:Long):Option[Supply] = {
    if(preload)
      return super.?(id,ts0,ts1)

    // generate list of dirs by days
    None
  }

}