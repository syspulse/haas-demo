package io.syspulse.haas.circ.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.util.Util

import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation

import os._

// Preload from file during start
class CirculationSupplyStoreDir(dir:String = "store/",preload:Boolean = false) extends CirculationSupplyStoreMem {

  if(preload) {
    val dd = load(dir)
    dd.foreach(d => this.+(d))

    log.info(s"Loaded store: ${size}")
  }

  def load(dir:String) = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    val dd = os.walk(storeDir)
      .filter(_.toIO.isFile())
      .map(f => {
        log.info(s"Loading file: ${f}")
        os.read(f)
      })
      .map(data => parse(data))
      .flatten

    dd
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

  override def ?(id:CirculationSupply.ID,ts0:Long,ts1:Long):List[CirculationSupply] = {
    if(preload)
      return super.?(id,ts0,ts1)

    // generate list of dirs by days
    List()
  }

}