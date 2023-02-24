package io.syspulse.haas.core

import scala.jdk.CollectionConverters._
import io.syspulse.skel.Ingestable

case class Blockchain(
  id:Blockchain.ID,
  cid:Option[String] = None  // chain_id 
)

object Blockchain {
  type ID = String
}