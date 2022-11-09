package io.syspulse.haas.core.serde

import scala.jdk.CollectionConverters._

import scala.util.Random

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.core.Token

object TokenJson extends DefaultJsonProtocol {
  
  implicit val jf_token = jsonFormat6(Token.apply _)
}
