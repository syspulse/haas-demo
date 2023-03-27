package io.syspulse.haas.ingest.eth

import com.typesafe.scalalogging.Logger

case class Config(  
  host:String="0.0.0.0",
  port:Int=8080,
  uri:String = "/api/v1/eth",
  
  feed:String = "",
  output:String = "",

  feedBlock:String = "",
  feedTx:String = "",
  feedTransfer:String = "",
  feedLog:String = "",
  feedMempool:String = "",

  outputBlock:String = "",
  outputTx:String = "",
  outputTransfer:String = "",
  outputLog:String = "",
  outputMempool:String = "",
  
  abi:String = "abi/",
  source:String="",
  
  size:Long = Long.MaxValue,
  limit:Long = Long.MaxValue,

  freq: Long = 0L,
  delimiter:String = "\n",
  
  // created dir: /mnt/s3/data/dev/ethereum/raw/csv/transactions/2016/12/14
  // Exception in thread "main" akka.stream.scaladsl.Framing$FramingException: Read 1048858 bytes which is more than 1048576 without seeing a line terminator
  buffer:Int = 5 * 1024*1024, 

  throttle:Long = 0L,
  
  entity:Seq[String] = Seq("tx"),
  
  expr:String = "",
  
  datastore:String = "dir://store",

  filter:Seq[String] = Seq(),

  cmd:String = "ingest",
  params: Seq[String] = Seq(),
  sinks:Seq[String] = Seq()
)
