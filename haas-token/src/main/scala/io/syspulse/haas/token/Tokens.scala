package io.syspulse.haas.token

import scala.collection.immutable

import io.jvm.uuid._

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

final case class Tokens(users: immutable.Seq[Token])

final case class TokenCreateReq(id: String, symbol:String, name:String, address:Option[String] = None, ts:Long = System.currentTimeMillis())
final case class TokenRandomReq()
final case class TokenActionRes(status: String,id:Option[ID])
final case class TokenRes(user: Option[Token])