package io.syspulse.haas.token.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl
import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.searches.SearchResponse

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

import io.syspulse.haas.token.elastic.TokenScan
import io.syspulse.haas.token.elastic.TokenSearch
import io.syspulse.skel.ingest.uri.ElasticURI

class TokenStoreElastic(uri:String) extends TokenStore {
  private val log = Logger(s"${this}")

  val elasticUri = ElasticURI(uri)

  implicit object TokenHitReader extends HitReader[Token] {
    // becasue of VID case class, it is converted unmarchsalled as Map from Elastic (field vid.id)
    override def read(hit: Hit): Try[Token] = {
      val source = hit.sourceAsMap
      Success(Token(
        source("id").toString, 
        source("symbol").toString,
        source("name").toString,
        source.get("contractAddress").map(_.toString),
        source.get("category").map(_.asInstanceOf[List[String]]).getOrElse(List()),
        source.get("icon").map(_.toString)
      ))
    }
  }
  
  val client = ElasticClient(JavaClient(ElasticProperties("http://"+elasticUri.uri)))

  import ElasticDsl._  
  def all:Seq[Token] = {    
    val r = client.execute {
      ElasticDsl
      .search(elasticUri.index)
      .matchAllQuery()
    }.await

    log.info(s"r=${r}")
    r.result.to[Token].toList
  }

  // slow and memory hungry !
  def size:Long = {
    val r = client.execute {
      ElasticDsl.count(Indexes(elasticUri.index))
    }.await
    r.result.count
  }

  def +(yell:Token):Try[TokenStore] = { 
    Failure(new UnsupportedOperationException(s"not implemented: ${yell}"))
  }

  def del(id:ID):Try[TokenStore] = { 
    Failure(new UnsupportedOperationException(s"not implemented: ${id}"))
  }

  def -(yell:Token):Try[TokenStore] = {     
    Failure(new UnsupportedOperationException(s"not implemented: ${yell}"))
  }

  def ?(id:ID):Option[Token] = {
    search(id.toString).headOption
  }

  def ??(txt:String):List[Token] = {
    search(txt)
  }

  def scan(txt:String):List[Token] = {
    val r = client.execute {
      ElasticDsl
        .search(elasticUri.index)
        .rawQuery(s"""
    { 
      "query_string": {
        "query": "${txt}",
        "fields": ["symbol", "name", "contractAddress","category","icon"]
      }
    }
    """)
    }.await

    log.info(s"r=${r}")
    r.result.to[Token].toList
  }

  def search(txt:String):List[Token] = {   
    val r = client.execute {
      com.sksamuel.elastic4s.ElasticDsl
        .search(elasticUri.index)
        .query(txt)
    }.await

    log.info(s"r=${r}")
    r.result.to[Token].toList
    
    // r match {
    //   case failure: RequestFailure => List.empty
    //   case results: RequestSuccess[SearchResponse] => r.as[Token] //results.result.hits.hits.toList
    //   case results: RequestSuccess[_] => results.result
    // }
  }

  def grep(txt:String):List[Token] = {
    val r = client.execute {
      ElasticDsl
        .search(elasticUri.index)
        .query {
          ElasticDsl.wildcardQuery("symbol",txt)
        }
    }.await

    log.info(s"r=${r}")
    r.result.to[Token].toList
  }

  def typing(txt:String):List[Token] = {  
    val r = client.execute {
      ElasticDsl
        .search(elasticUri.index)
        .rawQuery(s"""
    { "multi_match": { "query": "${txt}", "type": "bool_prefix", "fields": [ "symbol", "symbol._3gram", "name", "contractAddress", "contractAddress._3gram"] }}
    """)        
    }.await
    
    log.info(s"r=${r}")
    r.result.to[Token].toList
  }
}
