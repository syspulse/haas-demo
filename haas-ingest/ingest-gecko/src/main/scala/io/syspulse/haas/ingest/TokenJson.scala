package io.syspulse.haas.ingest

import scala.jdk.CollectionConverters._

import scala.util.Random

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.core.Token

object TokenJson extends DefaultJsonProtocol {
  implicit val fmt = jsonFormat4(Token.apply _)
}
