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
    try {
      val coin = data.parseJson.convertTo[CoingeckoCoinInfo]
      Seq(coin)
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'")
        Seq()
    }
  }

  def transform(cg: CoingeckoCoinInfo): Seq[Token] = {    
    Seq(Token(cg.id,cg.symbol,cg.name,cg.contract_address))
  }
}