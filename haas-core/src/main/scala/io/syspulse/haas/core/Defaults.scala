package io.syspulse.haas.core

import scala.jdk.CollectionConverters._

object Defaults {
  val UNI = "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"
  val RBN = "0x6123b0049f904d730db3c36a31167d9d4121fa6b"

  val TOKEN_SET = Map (
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
    // "ribbon-finance" -> "AAVE",
    "solana" -> "SOL",
    // "uniswap" -> "AAVE",
    UNI -> "UNI",
    RBN -> "RBN",

  )
}