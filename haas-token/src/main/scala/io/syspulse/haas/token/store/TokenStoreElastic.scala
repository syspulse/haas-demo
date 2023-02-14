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
import io.syspulse.skel.uri.ElasticURI
import io.syspulse.haas.token.server.Tokens

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
        source.get("addr").map(_.toString),
        source.get("cat").map(_.asInstanceOf[List[String]]).getOrElse(List()),
        source.get("icon").map(_.toString),
        source.get("src").map(_.asInstanceOf[Long])
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
  
  def ???(from:Option[Int],size:Option[Int]):Tokens = {
    val r = client.execute {
      ElasticDsl
      .search(elasticUri.index)
      .matchAllQuery()
      .from(from.getOrElse(0))
      .size(size.getOrElse(10))
    }.await

    log.info(s"r=${r}")
    Tokens(r.result.to[Token].toList,Some(r.result.totalHits))
  }

  def +(t:Token):Try[TokenStore] = { 
    Failure(new UnsupportedOperationException(s"not implemented: ${t}"))
  }

  def del(id:ID):Try[TokenStore] = { 
    Failure(new UnsupportedOperationException(s"not implemented: ${id}"))
  }

  def ?(id:ID):Try[Token] = {
    val r = { client.execute { 
      ElasticDsl
        .search(elasticUri.index)
        .termQuery(("id",id))
    }}.await

    r.result.to[Token].toList match {
      case r :: _ => Success(r)
      case _ => Failure(new Exception(s"not found: ${id}"))
    }
  }

  def ??(txt:String,from:Option[Int],size:Option[Int]):Tokens = {
    search(txt,from,size)
  }

  def scan(txt:String,from:Option[Int],size:Option[Int]):Tokens = {
    val r = client.execute {
      ElasticDsl
        .search(elasticUri.index)
        .from(from.getOrElse(0))
        .size(size.getOrElse(10))
        .rawQuery(s"""
    { 
      "query_string": {
        "query": "${txt}",
        "fields": ["symbol", "name", "addr","cat", "icon"]
      }
    }
    """)
    }.await

    log.info(s"r=${r}")
    Tokens(r.result.to[Token].toList,Some(r.result.totalHits))
  }

  def search(txt:String,from:Option[Int],size:Option[Int]):Tokens = {   
    val r = client.execute {
      com.sksamuel.elastic4s.ElasticDsl
        .search(elasticUri.index)
        .from(from.getOrElse(0))
        .size(size.getOrElse(10))
        .query(txt)
    }.await

    log.info(s"r=${r}")
    Tokens(r.result.to[Token].toList,Some(r.result.totalHits))
    
    // r match {
    //   case failure: RequestFailure => List.empty
    //   case results: RequestSuccess[SearchResponse] => r.as[Token] //results.result.hits.hits.toList
    //   case results: RequestSuccess[_] => results.result
    // }
  }

  def grep(txt:String,from:Option[Int],size:Option[Int]):Tokens = {
    val r = client.execute {
      ElasticDsl
        .search(elasticUri.index)
        .from(from.getOrElse(0))
        .size(size.getOrElse(10))
        .query {
          ElasticDsl.wildcardQuery("symbol",txt)
        }
    }.await

    log.info(s"r=${r}")
    Tokens(r.result.to[Token].toList,Some(r.result.totalHits))
  }

  def typing(txt:String,from:Option[Int],size:Option[Int]):Tokens = {  
    val r = client.execute {
      ElasticDsl
        .search(elasticUri.index)
        .from(from.getOrElse(0))
        .size(size.getOrElse(10))
        .rawQuery(s"""
    { "multi_match": { "query": "${txt}", "type": "bool_prefix", "fields": [ "symbol", "symbol._3gram", "name", "addr", "addr._3gram"] }}
    """)        
    }.await
    
    log.info(s"r=${r}")
    Tokens(r.result.to[Token].toList,Some(r.result.totalHits))
  }
}
