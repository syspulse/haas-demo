package io.syspulse.haas.ingest.price.feed

import scala.jdk.CollectionConverters._

import io.syspulse.skel.Ingestable
import io.syspulse.skel.util.Util

// {"jsonrpc":"2.0","id":1,"result":"0x0000000000000000000000000000000000000000000000000000002653f01440"}

// addr and ts are never in response !
case class ChainlinkPrice(jsonrpc:String,id:String,result:String,addr:Option[String] = None,ts:Option[Long] = None) extends Ingestable
