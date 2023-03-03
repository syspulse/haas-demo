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

class TokenStoreDir(dir:String = "store/") extends TokenStoreMem {
  import CoinGeckoTokenJson._

  @volatile var loading = false

  load(dir)

  def load(dir:String) = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    loading = true
    val vv = os.walk(storeDir)
      .filter(_.toIO.isFile())
      .map(f => {
        log.info(s"Loading file: ${f}")
        os.read(f)
      })
      .map(fileData => fileData.split("\n").map { data =>
        try {
          val cg = data.parseJson.convertTo[CoinInfo]
          log.debug(s"coin=${cg}")
          Seq(cg.toToken)
        } catch {
          case e:Exception => 
            log.error(s"could not parse data: ${data}",e); 
            Seq()
        }
      })
      .flatten // file
      .flatten // files

    vv.foreach(v => this.+(v))

    loading = false

    log.info(s"Loaded store: ${size}")
  }

}