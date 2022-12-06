package io.syspulse.haas.ingest.eth.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger

import os._

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.ingest.eth.intercept._
import io.syspulse.haas.ingest.eth.intercept.Interception.ID

// Preload from file during start
class InterceptionStoreDir(dir:String = "store/") extends InterceptionStoreMem {
  import InterceptionJson._

  load(dir)

  def load(dir:String) = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    val vv = os.walk(storeDir)
      .filter(_.toIO.isFile())
      .map(f => {
        log.info(s"Loading file: ${f}")
        os.read(f)
      })
      .map(data => {
        try {
          val i = data.parseJson.convertTo[Interception]
          log.debug(s"interception=${i}")
          Seq(i)
        } catch {
          case e:Exception => log.error(s"could not parse data: ${data}",e); Seq()
        }
      })
      .flatten

    vv.foreach(v => this.+(v))

    log.info(s"Loaded store: ${size}")
  }

}