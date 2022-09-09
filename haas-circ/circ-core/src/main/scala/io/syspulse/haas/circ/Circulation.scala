package io.syspulse.haas.circ

import scala.jdk.CollectionConverters._
import io.jvm.uuid._
import scala.math.BigInt

//import io.syspulse.skel.Ingestable
import io.syspulse.haas.core.Token

case class Holder(addr:String,value:BigInt)

case class Circulation(
  id:Circulation.ID,
  tid:Token.ID,
  
  name:String = "", 

  ts:Long = System.currentTimeMillis(),
  totalSupply:BigInt = 0,
  supply:BigInt = 0,

  holders:Seq[Holder] = Seq.empty
  ) {//extends Ingestable {
  
  def getKey:Option[Any] = Some(id)
}

object Circulation {
  type ID = UUID
}