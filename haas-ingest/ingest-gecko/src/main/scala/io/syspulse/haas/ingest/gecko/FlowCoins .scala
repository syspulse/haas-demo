package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._

import com.typesafe.scalalogging.Logger

import spray.json._

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.ingest.IngestFlow

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

trait FlowCoins {
  implicit val log = Logger(s"${this}")

  import CoingeckoJson._

  def tokensFilter:Seq[String] = Seq.empty

  def parse(data:String):Seq[CoingeckoCoin] = {
    try {
      val bulk = data.parseJson.convertTo[List[CoingeckoCoin]]
      if(tokensFilter.size == 0)
        bulk.toSeq
      else
        bulk.filter( c => tokensFilter.contains(c.id) || tokensFilter.map(_.toLowerCase).contains(c.symbol.toLowerCase) ).toSeq
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'")
        Seq()
    }
  }

  def transform(cg: CoingeckoCoin): Seq[Token] = {
    Seq(Token(cg.id,cg.symbol,cg.name,None))
  }

}