package io.syspulse.haas.holder.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger

import io.syspulse.haas.core.{Holder,Holders}
import io.syspulse.haas.core.Holders.ID

import os._

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.haas.core.DataSource
import io.syspulse.skel.store.StoreDir

import io.syspulse.haas.serde.HoldersJson._
import io.syspulse.haas.holder.server.Holderss
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneOffset
import io.syspulse.skel.util.Util

object HoldersStoreDir {
  import scala.util.matching._

  val FILE_NAME_1 = "holders.csv"
  val FILE_NAME_2 = "holders.json"

  val dirRegexp = """.*\/0x([0-9a-fA-F]+)\/holders\/(\d{4})\/(\d{2})\/(\d{2})\/.*""".r
  //val zdf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss_SSSZ")
  val df = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss_SSS")
  
  def toTimestamp(yyyy:String,mm:String,dd:String) = 
    LocalDateTime.parse(s"${yyyy}-${mm}-${dd}_235959_999",df).toInstant(ZoneOffset.UTC).toEpochMilli
    //ZonedDateTime.parse("${yyyy}-${mm}-${dd}_235959_999+0000",zdf).toInstant().toEpochMilli

  def parsePath(f:String):Try[(String,Long)] = {
    f match {
      case dirRegexp(addr,yyyy,mm,dd) =>
        Success((s"0x${addr.toLowerCase}",toTimestamp(yyyy,mm,dd)))
      case _ => 
        //Failure(new Exception(s"could not parse: ${f}"))
        Success((s"0x0",0L))
    }    
  }
}

class HolderStoreDir(dir:String = "store/",limit:Int=100) extends StoreDir[Holders,ID](dir) with HolderStore {

  val store = new HolderStoreMem()

  def toKey(id:String) = id

  def all:Seq[Holders] = store.all
  def size:Long = store.size

  // does not support writing add
  override def +(h:Holders):Try[Holders] = {
    store.+(h)    
  }

  override def ?(id:ID):Try[Holders] = store.?(id)

  def ???(id:ID,ts0:Option[Long],ts1:Option[Long],from:Option[Int],size:Option[Int],limit:Option[Int]):Holderss = 
    store.???(id,ts0,ts1,from,size,limit)

  override def load(dir:String,hint:String="") = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir} (limit=$limit)")

    //val hh:Seq[Holders] = os
    val r = os    
      .walk(storeDir)
      .filter(_.toIO.isFile())
      .filter( f => {
        log.debug(s"${f}: ${f.toIO.getName}")
        f.toIO.getName == HoldersStoreDir.FILE_NAME_1 || f.toIO.getName == HoldersStoreDir.FILE_NAME_2
      })
      .map( f => 
        HoldersStoreDir.parsePath(f.toString()) match {
          case Success((token,ts)) => Success((f,token,ts))
          case f @ Failure(e) => 
            log.error(s"could not parse path: ${f.toString}")
            Failure(e)
        }
        //Success((f,"0x11",0L))
      )
      .filter(_.isSuccess)
      .map(_.get)
      .foreach { case (f,token,ts) => {
        // this must be foreach and return Unit to avoid OOM !
        log.info(s"Loading file: ${f} (${ts} = ${Util.timestamp(ts,"yyyy-MM-dd'T'HH:mm:ssZ",java.time.ZoneId.of("UTC"))})")
        val data = os.read(f)
        
        val holders = parseHolders(data,token,ts,limit)
        
        // val hh = holders.take(limit)
        // val h = Holders(ts,token,hh,total = holders.size)
        // this.+(h)

        holders.foreach{ h => this.+(h)}        
        
      }}      
      
    store.holders.foreach{ hh =>
      log.info(s"Holders: ${hh._1}: ${hh._2.size}: total = ${hh._2.foldLeft(0L)((i,h) => i + h.holders.size)}")
    }
    
  }

  def parseHolders(lines:String,token:String,ts:Long,limit:Int):Seq[Holders] = {
    ts match {
      case 0L => parseHoldersJSON(lines,token,ts,limit)
      case _ => parseHoldersCSV(lines,token,ts,limit)
    }
  }

  def parseHoldersCSV(lines:String,token:String,ts:Long,limit:Int):Seq[Holders] = {
    // ignore possible header
    val hh = lines
      .split("[\r\n]")
      .filter(!_.trim.isEmpty())
      .filter(!_.startsWith("address,"))
      .flatMap(data => { 
        try {
          data.split(",",-1).toList match {
            case addr :: balance :: Nil => 
              Seq(Holder(addr,Util.toBigInt(balance)))
            case _ => 
              Seq()
          }
        } catch {
          case e:Exception => 
            log.error(s"could not parse data: ${data}",e); 
            Seq()
        }
      }).sorted.toSeq
    
    Seq(Holders(ts,token,hh,total = hh.size))
  }

  def parseHoldersJSON(lines:String,token:String,ts:Long,limit:Int):Seq[Holders] = {
    // ignore possible header
    lines
      .split("[\r\n]")
      .filter(!_.trim.isEmpty())
      .filter(_.startsWith("{"))
      .flatMap(data => {        
        // assume Json file
        try {
          import io.syspulse.haas.holder.store.HoldersFormatJson._
          val j = data.parseJson.convertTo[io.syspulse.haas.holder.store.Holders]

          val hh = j.holders.map( h => {
            Holder(h._1,h._2)
          }).sorted.toSeq

          log.info(s"holders: ${j.ts}: ${j.token}")
          Some(Holders(j.ts,j.token,hh,total = hh.size))
        } catch {
          case e:Exception => 
            log.error(s"could not parse data: ${data}",e); 
            Seq()
        }
      }).toIndexedSeq
  }

  // preload
  load(dir)
}