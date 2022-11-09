package io.syspulse.haas.circ

import scala.jdk.CollectionConverters._
import io.jvm.uuid._
import scala.math.BigInt

// compress fields because there can be a lot of them
// v = value
// r = ratio (percentage)
// lb = labels
case class SupplyHolder(addr:String, v:BigInt, r:Double, lb:List[String] = List())
