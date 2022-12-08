package io.syspulse.haas.circ.store

import scala.jdk.CollectionConverters._

import scala.util.Random

import io.jvm.uuid._

import io.syspulse.skel.service.JsonCommon
import spray.json._
import DefaultJsonProtocol._


case class Lock(address:String,quantity:String,ratio:Option[Double]=None)
case class Holder(address:String,quantity:String,ratio:Option[Double]=None)
case class Circulating(ts:Option[Long],totalSupply:String,circulatingSupply:String,locks:List[Lock],totalHolders:Long,topHolders:List[Holder],tokenId:Option[String] = None)

object CirculatingJson extends JsonCommon {
  import DefaultJsonProtocol._

  implicit val jf_l = jsonFormat3(Lock.apply _)
  implicit val jf_h = jsonFormat3(Holder.apply _)
  implicit val jf_c = jsonFormat7(Circulating.apply _)
}
