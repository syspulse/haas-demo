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

// Preload from file during start
class TokenStoreDir(dir:String = "store/") extends TokenStoreMem {
  import CoingeckoJson._

  load(dir)

  def load(dir:String) = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    val vv = os.list(storeDir)
      .map(f => {
        log.info(s"Loading file: ${f}")
        os.read(f)
      })
      .map(data => {
        try {
          val coin = data.parseJson.convertTo[CoinInfo]
          log.debug(s"coin=${coin}")
          Seq(Token(coin.id,coin.symbol,coin.name,coin.contract_address,category = coin.categories,icon = Option(coin.image.large)))
        } catch {
          case e:Exception => log.error(s"could not parse data: ${data}",e); Seq()
        }
      })
      .flatten

    vv.foreach(v => this.+(v))

    log.info(s"Loaded store: ${size}")
  }

}