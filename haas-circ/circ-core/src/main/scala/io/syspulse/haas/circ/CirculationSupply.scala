package io.syspulse.haas.circ

import scala.jdk.CollectionConverters._
import io.jvm.uuid._
import scala.math.BigInt

//import io.syspulse.skel.Ingestable
import io.syspulse.haas.core.Token

case class SupplyBucket(
  label:String,  
  value:BigInt,  
  ratio:Double,
  change:Double
)

case class Circulation(
  ts:Long = System.currentTimeMillis(),
  totalSupply:BigInt = 0,
  supply:BigInt = 0,
  inflation:Double = 0.0,

  buckets: List[SupplyBucket] = List(),

  holders:Seq[SupplyHolder] = Seq.empty,
  holdersTotal:Long = 0L,
  holdersUp:Long = 0L,
  holdersDown:Long = 0L
)

case class CirculationSupply(
  id: CirculationSupply.ID,
  tid:Token.ID,  
  name:String = s"Supply",
  history:List[Circulation] = List()

  ) {//extends Ingestable {
  
  def getKey:Option[Any] = Some(id)
}

object CirculationSupply {
  type ID = UUID

  def apply(id:String) = UUID(id)
}

