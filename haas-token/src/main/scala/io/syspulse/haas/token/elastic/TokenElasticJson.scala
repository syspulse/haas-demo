package io.syspulse.haas.token.elastic

import scala.jdk.CollectionConverters._

import scala.util.Random

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.serde.TokenJson
import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

object TokenElasticJson extends  DefaultJsonProtocol {
  import TokenJson._
  implicit val fmt = TokenJson.jf_tok_tok //jsonFormat9(Token.apply _)
}

object TokenElastic {
  import TokenElasticJson._  
  
  def toElastic(o:Token) = o.toJson
  def fromElastic(json:String) = json.parseJson.convertTo[Token]
}