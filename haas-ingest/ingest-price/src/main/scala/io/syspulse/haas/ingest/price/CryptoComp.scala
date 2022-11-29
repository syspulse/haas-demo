package io.syspulse.haas.ingest.price

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util

case class CryptoCompData(`FROMSYMBOL`:String,`PRICE`:Double,`LASTUPDATE`:Long)

case class CryptoCompUSD(`USD`:CryptoCompData)

case class CryptoComp(`RAW`:Map[String,CryptoCompUSD]) extends Ingestable
