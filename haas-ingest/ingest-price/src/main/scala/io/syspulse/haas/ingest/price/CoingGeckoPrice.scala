package io.syspulse.haas.ingest.price

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util

// {"ribbon-finance":{"usd":0.261621},"uniswap":{"usd":6.91}}

// ts is never in response !
case class CoinGeckoPrice(pairs:Map[String,Map[String,Double]],ts:Option[Long]=None) extends Ingestable
