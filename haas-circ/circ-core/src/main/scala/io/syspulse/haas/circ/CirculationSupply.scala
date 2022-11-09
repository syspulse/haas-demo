package io.syspulse.haas.circ

import scala.jdk.CollectionConverters._
import io.jvm.uuid._
import scala.math.BigInt

//import io.syspulse.skel.Ingestable
import io.syspulse.haas.core.Token

case class Circulation(
  ts:Long = System.currentTimeMillis(),
  totalSupply:BigInt = 0,
  supply:BigInt = 0,
  holders:Seq[Holder] = Seq.empty 
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

