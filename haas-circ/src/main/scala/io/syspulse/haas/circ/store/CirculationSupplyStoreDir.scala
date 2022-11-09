package io.syspulse.haas.circ.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger

import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation

import os._

// Preload from file during start
class CirculationSupplyStoreDir(dir:String = "store/") extends CirculationSupplyStoreMem {

  load(dir)

  def load(dir:String) = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    val vv = os.list(storeDir)
      .map(f => {
        log.info(s"Loading file: ${f}")
        os.read(f)
      })
      .map(data => parse(data))
      .flatten

    vv.foreach(v => this.+(v))

    log.info(s"Loaded store: ${size}")
  }

}