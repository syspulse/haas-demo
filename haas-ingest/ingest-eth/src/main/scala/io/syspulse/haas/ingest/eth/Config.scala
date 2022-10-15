package io.syspulse.haas.ingest.eth

import com.typesafe.scalalogging.Logger

case class Config(  
  
  feed:String = "",
  output:String = "",

  scripts:Seq[String]=Seq(),
  abi:String = "abi/",
  source:String="",
  
  size:Long = Long.MaxValue,
  limit:Long = Long.MaxValue,

  freq: Long = 0L,
  delimiter:String = "\n",
  buffer:Int = 1024*1024,
  throttle:Long = 0L,
  
  entity:String = "tx",
  
  expr:String = "",
  
  datastore:String = "stdout",

  filter:Seq[String] = Seq(),

  cmd:String = "ingest",
  params: Seq[String] = Seq(),
  sinks:Seq[String] = Seq()
)
