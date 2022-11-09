package io.syspulse.haas.token.elastic

import scala.jdk.CollectionConverters._

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.elastic.ElasticScan

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

trait TokenScan extends ElasticScan[Token] {

  import io.syspulse.haas.token.elastic.TokenElasticJson._
  implicit val fmt = TokenElasticJson.fmt 

  override def getSearchParamas():Map[String,String] = Map(
          "query" -> s""" {"match_all": {}} """,
          "_source" -> """ ["id", "symbol", "name", "contractAddress","category","icon"] """
        )
}