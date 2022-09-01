package io.syspulse.haas.token.elastic

import scala.jdk.CollectionConverters._
import io.syspulse.skel
import io.syspulse.skel.util.Util

import io.syspulse.skel.elastic.ElasticSearch

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

trait TokenSearch extends ElasticSearch[Token] {

  import io.syspulse.haas.token.elastic.TokenElasticJson._
  implicit val fmt = TokenElasticJson.fmt 

  def getWildcards(txt:String) = s"""
    { 
      "query_string": {
        "query": "${txt}",
        "fields": ["symbol", "address"]
      }
    }
    """

  def getSearches(txt:String) = s"""
    { "multi_match": { "query": "${txt}", "fields": [ "symbol", "address" ] }}
    """

  def getSearch(txt:String) = s"""
    { "match": { "symbol": "${txt}" }}
    """

  def getTyping(txt:String) = 
    s"""
    { "multi_match": { "query": "${txt}", "type": "bool_prefix", "fields": [ "symbol", "address._3gram" ] }}
    """
}