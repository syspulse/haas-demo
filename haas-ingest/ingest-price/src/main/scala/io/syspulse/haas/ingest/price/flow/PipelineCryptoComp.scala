package io.syspulse.haas.ingest.price.flow

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import akka.actor.ActorSystem
import akka.util.ByteString
import akka.http.javadsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow

import io.syspulse.skel
import io.syspulse.skel.config._
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import io.syspulse.skel.ingest._
import io.syspulse.skel.ingest.store._
import io.syspulse.skel.ingest.flow.Pipeline
import io.syspulse.skel.ingest.flow.Flows

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.skel.serde.Parq._
import com.github.mjakubowski84.parquet4s.{ParquetRecordEncoder,ParquetSchemaResolver}

import java.util.concurrent.TimeUnit

import io.syspulse.haas.core.Price
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.ingest.price.feed.CryptoCompPriceJson
import io.syspulse.haas.ingest.price._

import io.syspulse.haas.serde.PriceJson._
import io.syspulse.haas.ingest.price.PriceURI
import akka.stream.scaladsl.Framing
import io.syspulse.haas.serde.PriceDecoder
import io.syspulse.haas.core.resolver.TokenResolverMem

abstract class PipelineCryptoComp[T](feed:String,output:String)(implicit config:Config,parqEncoders:ParquetRecordEncoder[T],parsResolver:ParquetSchemaResolver[T]) 
  extends PipelinePrice[T](feed:String,output:String){
  
  val sourceID = DataSource.id("cryptocomp")
  val TOKENS_SLOT = "COINS"

  val idResolver = new TokenResolverMem(if(config.idResolver.isEmpty) None else Some(config.idResolver))

  override def resolve(tokens:Seq[String]) = 
    //tokens.flatMap(idResolver.resolve _).mkString(",")
    super.resolve(tokens.flatMap(idResolver.resolve _))

  // def apiSuffix():String = s"?fsyms=${TOKENS_SLOT}&tsyms=${config.tokensPair.mkString(",")}"
  def apiSuffix():String = s"?fsyms=${resolve(tokensFilter)}&tsyms=${config.tokensPair.mkString(",")}"
  
}
