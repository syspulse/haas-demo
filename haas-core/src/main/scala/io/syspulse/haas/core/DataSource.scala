package io.syspulse.haas.core

import scala.jdk.CollectionConverters._
import io.syspulse.skel.Ingestable

object DataSource {
  def id(src:String) = {
    src match {
      case "haas" => 0
      case "coingecko" => 1
      case "cryptocomp" => 20
      case "chainlink" => 99
      case _ => math.abs(src.hashCode)
    }
  }
}
