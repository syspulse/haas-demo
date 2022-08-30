package io.syspulse.haas.core

import scala.util.Random

import com.typesafe.scalalogging.Logger

import java.time.LocalDateTime
import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success

object Labels {

  val default = Seq(
    "binance" -> Label("binance","exchange"),
    "whale-1" -> Label("whale-1","whale"),
    "scam" -> Label("scam","scammer","0x0000001"),
    "multisig" -> Label("multisig","Multisig","Gnosis"),
    "hw" -> Label("hw","HSM","Hardware Wallet")
  )

  var labels = Map[String,Label]() ++ default

  def +(label:Label) = {
    labels = labels + (label.id -> label)
  }

}


