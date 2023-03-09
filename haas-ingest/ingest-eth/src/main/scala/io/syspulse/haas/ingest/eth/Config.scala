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
  feedToken:String = "",
  feedLog:String = "",
  feedMempool:String = "",

  outputBlock:String = "",
  outputTx:String = "",
  outputToken:String = "",
  outputLog:String = "",
  outputMempool:String = "",
  
  abi:String = "abi/",
  source:String="",
  
  size:Long = Long.MaxValue,
  limit:Long = Long.MaxValue,

  freq: Long = 0L,
  delimiter:String = "\n",
  buffer:Int = 1024*1024,
  throttle:Long = 0L,
  
  entity:Seq[String] = Seq("tx"),
  
  expr:String = "",
  
  datastore:String = "dir://store",

  filter:Seq[String] = Seq(),

  cmd:String = "ingest",
  params: Seq[String] = Seq(),
  sinks:Seq[String] = Seq()
)
