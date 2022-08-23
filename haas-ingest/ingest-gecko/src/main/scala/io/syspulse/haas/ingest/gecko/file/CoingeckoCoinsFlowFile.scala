package io.syspulse.haas.ingest.gecko.file

import scala.jdk.CollectionConverters._

import akka.stream.scaladsl.Sink
import akka.NotUsed

import io.syspulse.skel
import io.syspulse.skel.util.Util

import io.syspulse.skel.ingest.IngestFlow

import io.syspulse.haas.core.Token
import io.syspulse.haas.ingest.gecko._
import io.syspulse.haas.ingest.gecko.CoingeckoCoin
import io.syspulse.haas.ingest.gecko.CoingeckoFlow

class CoingeckoCoinsFlowFile(file:String)(uri:String,output:String,freq:Long,limit:Long,name:String = "",timeout:Long=5000L) 
  extends CoingeckoFlow[CoingeckoCoin](uri,output,freq,limit,name,timeout) 
  with CoingeckoCoinsFlow {
  
  //override def sink() = IngestFlow.toHiveFile(file)
}