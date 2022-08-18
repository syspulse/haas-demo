package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._

import com.typesafe.scalalogging.Logger

import spray.json._

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.ingest.IngestFlow
import io.syspulse.haas.core.Token

trait CoingeckoCoinInfoFlow {
  implicit val log = Logger(s"${this}")

  import CoingeckoJson._

  def parse(data:String):Seq[CoingeckoCoinInfo] = {
    val coin = data.parseJson.convertTo[CoingeckoCoinInfo]
    Seq(coin)
  }

  def transform(cg: CoingeckoCoinInfo): Token = {
    Token(cg.id,cg.symbol,cg.name,cg.contract_address)
  }
}