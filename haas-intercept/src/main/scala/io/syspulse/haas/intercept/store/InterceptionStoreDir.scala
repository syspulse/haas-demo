package io.syspulse.haas.intercept.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger

import os._

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.intercept._
import io.syspulse.haas.intercept.Interception.ID

// Preload from file during start
class InterceptionStoreDir(dir:String = "store/") extends InterceptionStoreMem {
  import InterceptionJson._

  @volatile
  var loading = false

  def load(dir:String) = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    val vv = os.walk(storeDir)
      .filter(_.toIO.isFile())
      .filter( f => {
        ! f.toIO.getName.toLowerCase.endsWith(".md")
      })
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

  def writeFile(ix:Interception) = os.write.over(os.Path(dir,os.pwd) / s"${ix.id}.json",ix.toJson.compactPrint)
  def delFile(ix:Interception) = os.remove(os.Path(dir,os.pwd) / s"${ix.id}.json")
  def delFile(id:Interception.ID) = os.remove(os.Path(dir,os.pwd) / s"${id}.json")

  override def +(ix:Interception):Try[InterceptionStore] = {     
    super.+(ix)

    if(loading) return Success(this)
    writeFile(ix)  
    Success(this)
  }

  override def del(id:Interception.ID):Try[InterceptionStore] = { 
    super.del(id)
    delFile(id)
    Success(this)
  }

  override def flush(ix:Option[Interception]):Try[InterceptionStore] = {
    ix match {
      case Some(ix) => writeFile(ix)
      case None => all.foreach(ix => writeFile(ix))
    }
    Success(this)
  }

  loading = true
  load(dir)
  loading = false
}