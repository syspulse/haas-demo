package io.syspulse.haas.circ.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger
import io.jvm.uuid._
import io.syspulse.skel.util.Util

import io.syspulse.haas.circ._

import spray.json._
import DefaultJsonProtocol._

import os._


// temporary structure to catch tokenId
  case class TokenCirculating(tokenId:String,circ:Circulation)

// Preload from file during start
class CirculationSupplyStoreDir(dir:String = "store/",preload:Boolean = true) extends CirculationSupplyStoreMem {
  import CirculatingJson._
  
  if(preload) {
    val dd = load(dir)
    dd.foreach(d => this.+(d))

    log.info(s"Loaded store: ${this.size}")
  }

  
  def load(dir:String):Seq[CirculationSupply] = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    val circs:Seq[TokenCirculating] = os.walk(storeDir)
      .filter(_.toIO.isFile())
      .filter( f => {
        log.debug(s"${f}: ${f.toIO.getName}")
        f.toIO.getName == "circulating_supply.json"
      })
      .map(f => {
        log.info(s"Loading file: ${f}")
        (f,os.read(f))
      })
      .flatMap{ case (f,data) => {
        parseCirculation(data).map{ case(tid,c) => TokenCirculating(tid,c)}
      }}
      
    log.info(s"Circulations: ${circs}")

    circs.groupBy(_.tokenId).map{ case(tid,circ) => {
      CirculationSupply(
        id = UUID.random,
        name = s"Uniswap",
        tokenId = tid,

        history = circ.map(_.circ).toList
      )
    }}.toSeq
    
  }

  def parseCirculation(data:String):Option[(String,Circulation)] = {
    try {
      val csFile = data.parseJson.convertTo[Circulating]
      
      val cs = Circulation(
        ts = csFile.timestamp.getOrElse(System.currentTimeMillis / 1000L) * 1000L,
        totalSupply = csFile.totalSupply,
        supply = csFile.circulatingSupply,
        inflation = csFile.inflation,
        
        buckets = csFile.locks.map(lock => SupplyBucket(label = lock.address,value = lock.quantity, ratio = lock.ratio.getOrElse(0.0))),
        
        holdersTotal = csFile.totalHolders,
        holders = csFile.topHolders.map(h => SupplyHolder(addr = h.address,v = h.quantity,r = h.ratio.getOrElse(0.0)))
      )

      Some((csFile.token_address.getOrElse(""),cs))
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

  override def ?(id:CirculationSupply.ID,ts0:Long,ts1:Long):Option[CirculationSupply] = {
    if(preload)
      return super.?(id,ts0,ts1)

    // generate list of dirs by days
    None
  }

}