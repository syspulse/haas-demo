package io.syspulse.haas.circ.store

import scala.jdk.CollectionConverters._

import scala.util.Random

import io.jvm.uuid._

import io.syspulse.skel.service.JsonCommon
import spray.json._
import DefaultJsonProtocol._


case class Lock(address:String,quantity:BigInt,ratio:Option[Double]=None,labels:List[String]=List())
case class Holder(address:String,quantity:BigInt,ratio:Option[Double]=None,labels:List[String]=List())
case class Circulating(token_address:Option[String],timestamp:Option[Long],totalSupply:BigInt,circulatingSupply:BigInt,inflation:Double,locks:List[Lock],totalHolders:Long,topHolders:List[Holder],tokenId:Option[String] = None)

object CirculatingJson extends JsonCommon {
  import DefaultJsonProtocol._

  implicit val jf_l = jsonFormat4(Lock.apply _)
  implicit val jf_h = jsonFormat4(Holder.apply _)
  implicit val jf_c = jsonFormat9(Circulating.apply _)
}
