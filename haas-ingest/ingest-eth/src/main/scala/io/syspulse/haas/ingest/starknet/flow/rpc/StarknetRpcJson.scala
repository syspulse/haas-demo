package io.syspulse.haas.ingest.starknet.flow.rpc

import com.typesafe.scalalogging.Logger

// ATTENTION
import io.syspulse.skel.service.JsonCommon
import spray.json._
import spray.json.{DefaultJsonProtocol,NullOptions}

object StarknetRpcJson extends JsonCommon {
  
  implicit val jf_rpc_tx = jsonFormat10(RpcTx)
  implicit val jf_rpc_res = jsonFormat8(RpcBlockResult)
  implicit val jf_rpc_bl = jsonFormat3(RpcBlock)

  implicit val jf_rpc_evn = jsonFormat3(RpcEvent)
  implicit val jf_rpc_evn_r = jsonFormat4(RpcEventsResult)
  implicit val jf_rpc_evns = jsonFormat3(RpcEvents)   
}
