
package io.syspulse.haas.ingest.mempool

import io.syspulse.skel.service.JsonCommon

import spray.json.DefaultJsonProtocol
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, deserializationError}
import spray.json.NullOptions

// with NullOptions only for writing
object MempoolJson extends JsonCommon {
  implicit val jf_MempoolTx = jsonFormat14(MempoolTx)
}

