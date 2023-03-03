package io.syspulse.haas.token.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

import os._

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.haas.ingest.coingecko.CoinInfo
import io.syspulse.haas.ingest.coingecko.CoinGeckoTokenJson
import io.syspulse.haas.ingest.coingecko._
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.core.TokenBlockchain

// always read-only
class TokenStoreResource(dir:String = "store/tokens.json") extends TokenStoreMem {
  import CoinGeckoTokenJson._

  load(dir)

  def load(storeFile:String) = {
    log.info(s"Loading store: resource://${storeFile}")
    val lines =scala.io.Source.fromResource(storeFile).getLines()
    
    val vv = lines.map( data => 
      try {
        // try to load as many single line jsons
        val cg = data.parseJson.convertTo[CoinInfo]
        log.debug(s"coin=${cg}")        
        Seq(cg.toToken)
      } catch {
        case e:Exception => log.error(s"could not parse data: ${data}",e); Seq()
      }
    ).flatten

    vv.foreach(v => this.+(v))

    log.info(s"Loaded store: ${size}")
  }

}