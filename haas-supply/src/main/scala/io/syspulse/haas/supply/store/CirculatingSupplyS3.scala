package io.syspulse.haas.supply.store

import scala.jdk.CollectionConverters._

import scala.util.Random

import io.jvm.uuid._

import io.syspulse.skel.service.JsonCommon
import spray.json._
import DefaultJsonProtocol._

case class TotalSupply(total:BigInt,dstr:Map[String,BigInt])
case class CircSupply(total:BigInt,dstr:Map[String,BigInt])

case class Category(total:BigInt,dstr:Map[String,BigInt])
case class Lock(tag:String,value:BigInt)

case class Holders(total:Long,dstr:Map[String,Long])

case class TV(total:BigInt,dstr:Map[String,BigInt])
case class TopHolders(addr:String,r:Double,tv:TV)

case class CirculatingSupply(
  token_id:String,
  token_address:Map[String,String],
  ts:Long,
  price:Double,

  totalSupply:TotalSupply,
  circSupply:CircSupply,
    
  categories:Map[String,Category],
  locks:Map[String,Map[String,Lock]],
  
  holders:Holders,  
  holdersGrowth:Holders,
  uniqueHoldersUp:Holders,
  uniqueHoldersDown:Holders,

  topHolders:Seq[TopHolders],
)

object CirculatingSupplyJson extends JsonCommon {
  import DefaultJsonProtocol._
  
  implicit val jf_cs_ts = jsonFormat2(TotalSupply.apply _)
  implicit val jf_cs_cs = jsonFormat2(CircSupply.apply _)

  implicit val jf_cs_cat = jsonFormat2(Category.apply _)
  implicit val jf_cs_l = jsonFormat2(Lock.apply _)
  implicit val jf_cs_h = jsonFormat2(Holders.apply _)

  implicit val jf_cs_tv = jsonFormat2(TV.apply _)
  implicit val jf_cs_th = jsonFormat3(TopHolders.apply _)
  
  implicit val jf_cs_circ = jsonFormat13(CirculatingSupply.apply _)
}
