package io.syspulse.haas.core

import scala.jdk.CollectionConverters._

object Defaults {
  val UNI = "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"
  val RBN = "0x6123b0049f904d730db3c36a31167d9d4121fa6b"
  val STETH = "0xae7ab96520de3a18e5e111b5eaab095312d7fe84"

  val TOKEN_SET = Map (
    "aave" -> "AAVE",
    "chainlink" -> "LINK",
    "dappnode"-> "NODE",
    "ethereum"-> "ETH",
    "ethereum-name-service"-> "ENS",
    "foam-protocol"-> "FOAM",
  
    //"pollen-coin"-> "PCN",    
    "solana" -> "SOL",

    "livepeer"-> "LPT",
    "matic-network"-> "MATIC",
    "near"-> "NEAR",
    "noia-network"-> "NOIA",
    "optimism"-> "OP",
    "helium"-> "HNT",
    "gmx"-> "GMX",
    "tether" -> "USDT",
    "usd-coin" -> "USDC",
    "illuvium" -> "ILV",
    "staked-ether" -> "stETH",
    "wrapped-bitcoin" -> "WBTC",
    "weth" -> "WETH",
    "hackenai" -> "HAI",
    "celer-network" -> "CELR",
    "synapse-2" -> "SYN",
    "looksrare" -> "LOOKS",

    "usd" -> "USD",
    
    UNI -> "UNI",
    RBN -> "RBN",
    STETH -> "stETH",

  )
}