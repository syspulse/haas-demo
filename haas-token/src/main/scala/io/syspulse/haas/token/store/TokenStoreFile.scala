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
import io.syspulse.haas.ingest.gecko.CoinInfo
import io.syspulse.haas.ingest.gecko.CoingeckoJson
import io.syspulse.haas.ingest.gecko._
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.core.TokenBlockchain

// Preload from file during start
class TokenStoreFile(file:String = "store/tokens.json") extends TokenStoreMem {
  import CoingeckoJson._

  load(file)

  def load(file:String) = {
    log.info(s"Loading file store: ${file}")

    val vv = scala.io.Source.fromFile(file).getLines()
      .map(data => {
        try {
          val cg = data.parseJson.convertTo[CoinInfo]
          log.debug(s"coin=${cg}")
          Seq(cg.toToken)
        } catch {
          case e:Exception => log.error(s"could not parse data: ${data}",e); Seq()
        }
      })
      .flatten

    vv.foreach(v => this.+(v))

    log.info(s"Loaded store: ${size}")
  }

}