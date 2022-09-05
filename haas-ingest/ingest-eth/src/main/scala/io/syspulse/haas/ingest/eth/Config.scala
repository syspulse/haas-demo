package io.syspulse.haas.ingest.eth

import com.typesafe.scalalogging.Logger

case class Config(  
  
  feed:String = "",
  output:String = "",

  script:String="",
  abi:String = "",
  source:String="",
  
  limit:Long = 0L,
  freq: Long = 0L,
  delimiter:String = "",
  buffer:Int = 0,
  throttle:Long = 0L,
  
  entity:String = "",
  
  expr:String = "",
  
  datastore:String = "",

  filter:Seq[String] = Seq(),

  cmd:String = "",
  params: Seq[String] = Seq(),
  sinks:Seq[String] = Seq()
)
