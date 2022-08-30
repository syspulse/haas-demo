package io.syspulse.haas.ingest.eth

import com.typesafe.scalalogging.Logger

// ATTENTION
import io.syspulse.skel.service.JsonCommon
import spray.json._
import spray.json.{DefaultJsonProtocol,NullOptions}
import io.syspulse.haas.core.Tx

object EthJson extends JsonCommon with NullOptions {
  import DefaultJsonProtocol._
  implicit val txJsonFormat = jsonFormat(Tx,"block_timestamp","transaction_index","hash","block_number","from_address","to_address","gas","gas_price","input","value")
}
