package io.syspulse.haas.core

import scala.jdk.CollectionConverters._
import io.syspulse.skel.Ingestable

case class Price(id:Token.ID, ts:Long, v:Double, pair:Option[Token.ID]=None, src:Long = -1) extends Ingestable {
  
  override def getKey:Option[Any] = Some(s"${id}:${ts.toString}")

  override def getId:Option[Any] = Some(id)
}

object Price {
  def id(src:String) = math.abs(src.hashCode)
}
