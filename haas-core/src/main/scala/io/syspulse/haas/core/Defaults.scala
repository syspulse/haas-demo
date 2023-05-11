package io.syspulse.haas.core

import scala.jdk.CollectionConverters._

object Defaults {
  val UNI = "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"
  val RBN = "0x6123b0049f904d730db3c36a31167d9d4121fa6b"
  val STETH = "0xae7ab96520de3a18e5e111b5eaab095312d7fe84"

  val TOKEN_SET = Map (
    //"solidly" -> "SOLID",
    "0x888ef71766ca594ded1f0fa3ae64ed2941740a20" -> "SOLID",

    //"kyber-network-crystal" -> "KNC",
    "0xdefa4e8a7bcba345f687a2f1456f5edd9ce97202" -> "KNC",
    //"balancer" -> "BAL",
    "0xba100000625a3754423978a60c9317c58a424e3d" -> "BAL",

    "verse-bitcoin" -> "VERSE",
    "0x249ca82617ec3dfb2589c4c17ab7ec9765350a18" -> "VERSE",
    
    "solidly-v2" -> "SOLID",
    "0x777172d858dc1599914a1c4c6c9fc48c99a60990" -> "SOLID",

    "aave" -> "AAVE",
    "chainlink" -> "LINK",
    // "dappnode"-> "NODE",
    "ethereum"-> "ETH",
    "ethereum-name-service"-> "ENS",
    // "foam-protocol"-> "FOAM",
  
    //"pollen-coin"-> "PCN",    
    // "solana" -> "SOL",

    // "livepeer"-> "LPT",
    "matic-network"-> "MATIC",
    // "near"-> "NEAR",
    // "noia-network"-> "NOIA",
    "optimism"-> "OP",
    // "helium"-> "HNT",
    "gmx"-> "GMX",
    "tether" -> "USDT",
    "usd-coin" -> "USDC",
    // "illuvium" -> "ILV",
    "staked-ether" -> "stETH",
    "wrapped-bitcoin" -> "WBTC",
    "weth" -> "WETH",
    "hackenai" -> "HAI",
    // "celer-network" -> "CELR",
    "synapse-2" -> "SYN",
    "looksrare" -> "LOOKS",

    "usd" -> "USD",
    
    UNI -> "UNI",
    "uniswap" -> "UNI",
    RBN -> "RBN",
    "ribbon-finance" -> "RBN",
    STETH -> "stETH",
    "staked-ether" -> "stETH"

  )
}