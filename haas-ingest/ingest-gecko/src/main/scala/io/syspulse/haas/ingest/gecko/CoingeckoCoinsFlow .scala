package io.syspulse.haas.ingest.gecko

import scala.jdk.CollectionConverters._

import com.typesafe.scalalogging.Logger

import spray.json._

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.ingest.IngestFlow
import io.syspulse.haas.core.Token

trait CoingeckoCoinsFlow {
  implicit val log = Logger(s"${this}")

  import CoingeckoJson._

  def tokensFilter:Seq[String] = Seq.empty

  def parse(data:String):Seq[CoingeckoCoin] = {
    val bulk = data.parseJson.convertTo[List[CoingeckoCoin]]
    if(tokensFilter.size == 0)
      bulk.toSeq
    else
      bulk.filter( c => tokensFilter.contains(c.id) || tokensFilter.map(_.toLowerCase).contains(c.symbol.toLowerCase) ).toSeq
  }

  def transform(cg: CoingeckoCoin): Token = {
    Token(cg.id,cg.symbol,cg.name,None)
  }

}