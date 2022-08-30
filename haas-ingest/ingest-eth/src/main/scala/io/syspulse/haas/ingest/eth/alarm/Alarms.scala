package io.syspulse.haas.ingest.eth.alarm

import com.typesafe.scalalogging.Logger

import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success

import io.syspulse.haas.ingest.eth.notify.NotficationDest


case class UserAlarm(id:String,to:NotficationDest)

