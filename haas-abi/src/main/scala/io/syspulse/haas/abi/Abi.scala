package io.syspulse.haas.abi

import scala.util.Random
import scala.jdk.CollectionConverters._
import scala.collection.immutable
import io.syspulse.skel.Ingestable

case class Abi (
  //id:String, 
  ent:Option[String] = None, 
  hex:Option[String]=None,
  tex:Option[String]=None,
  addr:Option[String] = None,
  json:Option[String] = None
)

final case class Abis(abis: immutable.Seq[Abi],total:Option[Long] = None)