package io.syspulse.haas.ingest.price

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util

case class CryptoCompData(`FROMSYMBOL`:String,`PRICE`:Double,`LASTUPDATE`:Long)

case class CryptoCompUSD(`USD`:CryptoCompData)

//case class CryptoComp(`RAW`:Map[String,CryptoCompUSD]) extends Ingestable
case class CryptoCompFull(`RAW`:Map[String,Map[String,CryptoCompData]]) extends Ingestable

// {
//   "UNI": {
//     "USD": 5.416
//   },
//   "RBN": {
//     "USD": 0.251
//   }
// }

// ts is never in response !
case class CryptoCompTerse(pairs:Map[String,Map[String,Double]],ts:Option[Long]=None) extends Ingestable
