package io.syspulse.haas.intercept.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger
import io.jvm.uuid._

import os._

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.intercept.script._
import io.syspulse.haas.intercept.script.Script.ID

// Preload from file during start
class ScriptStoreDir(dir:String = "scripts/") extends ScriptStoreMem {
  import ScriptJson._

  loadEngines(dir)

  def loadEngines(dir:String) = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    val vv = os.walk(storeDir)
      .filter(_.toIO.isFile())
      .map(f => {
        log.info(s"Loading file: ${f}")
        
        (f,os.read(f))
      })
      .map{ case (f,data) => {
        try {
          val ext = f.ext
          val id = f.last
          val src = data
          val s = ext.toLowerCase match {
            case "js" => {
              Seq(Script(id, "js", src, name = f.toString))
            }
            case t => log.error(s"Unknown script type: ${t}"); 
              Seq()
          }
          
          //val s = data.parseJson.convertTo[Script]
          s

        } catch {
          case e:Exception => log.error(s"could not parse data: ${data}",e); Seq()
        }
      }}
      .flatten

    vv.foreach(v => this.+(v))

    log.info(s"Loaded store: ${size}")
  }

}