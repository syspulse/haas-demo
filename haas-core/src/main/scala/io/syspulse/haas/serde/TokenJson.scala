package io.syspulse.haas.serde

import scala.jdk.CollectionConverters._

import scala.util.Random

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.core.{Token,TokenBlockchain}

object TokenJson extends DefaultJsonProtocol {
  
  implicit val jf_tb = jsonFormat2(TokenBlockchain.apply _)
  implicit val jf_token = jsonFormat9(Token.apply _)
}
