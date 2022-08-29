package io.syspulse.haas.ingest.gecko.file

import scala.jdk.CollectionConverters._
import com.typesafe.scalalogging.Logger

import akka.stream.scaladsl.Sink
import akka.NotUsed

import io.syspulse.skel
import io.syspulse.skel.util.Util

import io.syspulse.skel.ingest.IngestFlow
import io.syspulse.skel.elastic.ElasticFlow
import io.syspulse.skel.elastic.ElasticClient

import io.syspulse.haas.core.Token
import io.syspulse.haas.ingest.gecko._

import io.syspulse.haas.ingest.gecko.CoingeckoCoin
import io.syspulse.haas.ingest.gecko.CoingeckoFlow

import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSink
import akka.stream.alpakka.elasticsearch.ElasticsearchParams

import spray.json.JsonFormat

import io.syspulse.haas.token.elastic.TokenElasticJson

// class FlowElasticCoins(uri:String,output:String,freq:Long,limit:Long,name:String = "",timeout:Long=5000L) 
//   extends CoingeckoFlow[CoingeckoCoin](uri,output,freq,limit,name,timeout) 
//   with FlowCoins 
//   with ElasticClient[Token] {

//   override val log:Logger = Logger(s"${this}")

//   import TokenElasticJson.fmt._
  
//   def sink():Sink[WriteMessage[Token,NotUsed],Any] = 
//     ElasticsearchSink.create[Token](
//       ElasticsearchParams.V7(getIndexName()), settings = getSinkSettings()
//     )

//   def transform(t:Token):Seq[WriteMessage[Token,NotUsed]] = {
//     //log.debug(s"${Util.now} ${t}")    
//     Seq(WriteMessage.createIndexMessage(t.id, t))
//   }


// }

// object FlowElasticCoins {
//   def toElastic(uri:String) = {
//     new FlowElasticCoins(uri,"",0,0,"coins").sink()
//   }
// }