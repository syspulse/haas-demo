package io.syspulse.haas.core

import scala.jdk.CollectionConverters._
import io.syspulse.skel.Ingestable

case class Token(id:Token.ID,symbol:String,name:String, contractAddress:Option[String] = None) extends Ingestable {
  override def getKey:Option[Any] = Some(id)
}

object Token {
  type ID = String
}