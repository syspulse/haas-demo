package io.syspulse.haas.core

import scala.util.Random

import com.typesafe.scalalogging.Logger

import java.time.LocalDateTime
import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success

case class Label(id:String,typ:String = "",desc:String = "")
