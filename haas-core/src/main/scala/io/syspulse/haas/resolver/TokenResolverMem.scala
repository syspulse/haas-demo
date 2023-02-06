package io.syspulse.haas.core.resolver

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.{Duration,FiniteDuration}
import com.typesafe.scalalogging.Logger

import io.syspulse.skel
import io.syspulse.skel.util.Util
import io.syspulse.skel.config._

import java.util.concurrent.TimeUnit

class TokenResolverMem(datastore:Option[String] = None) extends Resolver[String,String] {
  val default = Map(
    "uniswap" -> "UNI",
    "ribbon-finance" -> "RBN",
    
    "aave" -> "AAVE",
    "chainlink" -> "LINK",
    "dappnode"-> "NODE",
    "ethereum"-> "ETH",
    "ethereum-name-service"-> "ENS",
    "foam-protocol"-> "FOAM",
    "gmx"-> "GMX",
    "helium"-> "HNT",
    "livepeer"-> "LPT",
    "matic-network"-> "MATIC",
    "near"-> "NEAR",
    "noia-network"-> "NOIA",
    "optimism"-> "OP",
    "pollen-coin"-> "PCN",
    "solana" -> "SOL",

    "usd" -> "USD",
  )

  val store = default ++ default.map{case(k,v) => v -> k} ++ (if(!datastore.isDefined) Map() else {
      val r = datastore.get.split("\n").flatMap(_.split(",").toList match {
        case id :: ticker :: Nil => Some(id.trim -> ticker.trim)
        case _ => None
      })
      r.toMap
    })

  def resolve(xid:String):Option[String] = store.get(xid)
  def resolveReverse(id:String):Option[String] = store.get(id.toLowerCase())
}

