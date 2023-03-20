package io.syspulse.haas.serde

import scala.jdk.CollectionConverters._
import com.typesafe.scalalogging.Logger

import spray.json._
import DefaultJsonProtocol._

import io.syspulse.haas.core.Price

class PriceDecoder extends Decoder[Price] {
  protected val log = Logger(s"${this}")

  import PriceJson._

  def parse(data:String):Seq[Price] = {
    if(data.isEmpty()) return Seq()
    try {
      if(data.stripLeading().startsWith("{")) {
        val price = data.parseJson.convertTo[Price]
        Seq(price)
      } else {
        // assume csv
        val price = data.split(",",-1).toList match {
          case id :: ts :: v :: pair :: xid :: Nil => 
            Some(Price(id,ts.toLong,v.toDouble,Option(pair),xid.toLong))
          case _ => {
            log.error(s"failed to parse: '${data}'")
            None
          }
        }
        price.toSeq
      }
    } catch {
      case e:Exception => 
        log.error(s"failed to parse: '${data}'",e)
        Seq()
    }
  }
}
