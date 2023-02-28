package io.syspulse.haas.ingest.price.flow

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import akka.actor.ActorSystem
import akka.util.ByteString

import akka.http.scaladsl.model.{HttpRequest,HttpMethods,HttpEntity,ContentTypes}
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
import java.util.concurrent.TimeUnit

import io.syspulse.haas.core.Price
import io.syspulse.haas.core.DataSource

import io.syspulse.haas.ingest.price._
import io.syspulse.haas.ingest.price.feed.ChainlinkPriceJson

import io.syspulse.haas.serde.PriceJson._
import io.syspulse.haas.ingest.price.PriceURI
import akka.stream.scaladsl.Framing
import io.syspulse.haas.serde.PriceDecoder
import io.syspulse.haas.core.resolver.TokenResolverMem
import akka.http.javadsl.model.ContentType

import io.syspulse.haas.ingest.price.feed.ChainlinkPrice
class PipelineChainlink(feed:String,output:String)(implicit config:Config) extends PipelinePrice[ChainlinkPrice](feed:String,output:String){
  
  import ChainlinkPriceJson._

  val sourceID = DataSource.id("chainlink")
  val TOKENS_SLOT = ""

  def apiSuffix():String = ""

  // Oracle Contracts -> TokenId
  val resolver = new TokenResolverMem(Some(
    """0x553303d460ee0afb37edff9be42922d8ff63220e,uniswap
    0xcfe54b5cd566ab89272946f602d76ea879cab4a8,staked-ether
    0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419,ribbon-finance""".toLowerCase()
  ))
  
  override def source(feed:String) = {
    feed.split("://").toList match {
      case "http" :: _ | "https" :: _ => {
        val reqs = 
          tokensFilter
          .flatMap(resolver.resolve(_))
          .zipWithIndex
          .map{ case(tokenAddress,i) => {
            val json = s"""
              {"jsonrpc":"2.0","method":"eth_call",
                "params":[{
                  "from": "0x0000000000000000000000000000000000000000",
                  "to": "${tokenAddress}", 
                  "data": "0x50d25bcd0000000000000000000000000000000000000000000000000000000000000000"}, 
                  "latest"],
                "id":"${tokenAddress}"}
              """.trim.replaceAll("\\s+","")

            log.debug(s"json='${json}'")
            
            HttpRequest( method = HttpMethods.POST, 
              uri = feed,
              entity = HttpEntity(ContentTypes.`application/json`,json)
            )
            .withHeaders(Accept(MediaTypes.`application/json`))
          }}
            
        log.info(s"reqs=${reqs}")

        val s = Source.tick(FiniteDuration(10,TimeUnit.MILLISECONDS), 
                            FiniteDuration(config.ingestCron.toLong,TimeUnit.SECONDS), 
                            s"ingest-${System.currentTimeMillis()}-${feed}")
        .map(h => {
          log.info(s"Cron --> ${h}")
          h
        })
        .via(
          Flows.fromHttpListAsFlow(reqs, par = 1, frameDelimiter = config.delimiter,frameSize = config.buffer, throttle = config.throttleSource)
        )
        s
      }
            
      case _ => super.source(feed)
    }
  }

  def parse(data:String):Seq[ChainlinkPrice] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        try {
          val obj = data.parseJson.convertTo[ChainlinkPrice]          
          //log.info(s"price=${price}")
          Seq(obj)

        } catch {
          case e:Exception => 
            log.error(s"failed to parse: '${data}'",e)
            Seq()
        }
      } else {
        val price = data.split(",").toList match {
          case rpc :: id :: result :: Nil => 
            Some(ChainlinkPrice(rpc,id,result))
            None
          case _ => {
            log.error(s"failed to parse: '${data}'")
            None
          }
        }
        //log.info(s"price=${price}")
        price.toSeq
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }

  def transform(cp: ChainlinkPrice): Seq[Price] = {
    
    //val token = tokensFilter(cp.id.toInt)
    // tokenAddress is populated into 'id'
    val token = resolver.resolve(cp.id.toLowerCase).getOrElse(cp.id)
    
    val ts = System.currentTimeMillis()
    val price = BigInt(Util.fromHexString(cp.result)).toDouble
    val p = config.priceFormat match {
      case "price" => Price(token, ts, price, pair = None, src = sourceID)
      //case "telemetry" => Price(s"${token}-${p}", cct.ts.getOrElse(0L), price, None, src = sourceID)      
    }
    Seq(p)
  }
}
