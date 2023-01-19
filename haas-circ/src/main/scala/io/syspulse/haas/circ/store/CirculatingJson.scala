package io.syspulse.haas.circ.store

import scala.jdk.CollectionConverters._

import scala.util.Random

import io.jvm.uuid._

import io.syspulse.skel.service.JsonCommon
import spray.json._
import DefaultJsonProtocol._


case class Info(label:String,desc:Option[String]=None)

case class Lock(addr:String,value:BigInt,r:Option[Double]=None,info:Option[List[Info]]=None)
case class Holder(addr:String,value:BigInt,r:Option[Double]=None,info:Option[List[Info]]=None)
case class Circulating(tokenAddress:Option[String],timestamp:Option[Long],totalSupply:BigInt,circulatingSupply:BigInt,inflation:Double,locks:List[Lock],totalHolders:Long,topHolders:List[Holder],tokenId:Option[String] = None,categories:Map[String,BigInt]=Map())

object CirculatingJson extends JsonCommon {
  import DefaultJsonProtocol._

  implicit val jf_i = jsonFormat2(Info.apply _)
  implicit val jf_l = jsonFormat4(Lock.apply _)
  implicit val jf_h = jsonFormat4(Holder.apply _)
  implicit val jf_c = jsonFormat10(Circulating.apply _)
}
