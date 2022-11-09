package io.syspulse.haas.circ.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger

import os._

import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation

// Preload from file during start
class CirculationSupplyStoreFile(file:String = "store/circs.json") extends CirculationSupplyStoreMem {

  load(file)

  def load(file:String) = {
    log.info(s"Loading file store: ${file}")

    val vv = scala.io.Source.fromFile(file).getLines()
      .map(data => parse(data))
      .flatten

    vv.foreach(v => this.+(v))

    log.info(s"Loaded store: ${size}")
  }

}