package io.syspulse.haas.circ.client

import scala.util.{Try,Success,Failure}
import scala.collection.immutable

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.settings.ClientConnectionSettings

//import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, ExecutionContext, Future}

import akka.NotUsed

import scala.concurrent.Future
import scala.util.{ Failure, Success }

import io.jvm.uuid._

import io.syspulse.skel.ClientHttp
import io.syspulse.skel.util.Util
import io.syspulse.skel.service.JsonCommon

import io.syspulse.haas.circ._
import io.syspulse.haas.circ.serde._
import io.syspulse.haas.circ.server.CirculationSupplyProto
import io.syspulse.haas.circ.server._
import io.syspulse.haas.circ.CirculationSupply

class CirculationSupplyClientHttp(uri:String)(implicit as:ActorSystem[_], ec:ExecutionContext) extends ClientHttp(uri)(as,ec) with CirculationSupplyService {
  
  import CirculationSupplyJson._
  import CirculationSupplyProto._
  import spray.json._
  
  def reqGetCirculationSupply(id:Circulation.ID) = HttpRequest(method = HttpMethods.GET, uri = s"${uri}/${id}")  
  def reqGetCirculationSupplys() = HttpRequest(method = HttpMethods.GET, uri = s"${uri}")
  
  def get(id:Circulation.ID):Future[Option[CirculationSupply]] = {
    log.info(s"${id} -> ${reqGetCirculationSupply(id)}")
    for {
      rsp <- Http().singleRequest(reqGetCirculationSupply(id))
      circ <- if(rsp.status == StatusCodes.OK) Unmarshal(rsp).to[Option[CirculationSupply]] else Future(None)
    } yield circ 
  }
  def all():Future[CirculationSupplys] = {
    log.info(s" -> ${reqGetCirculationSupplys()}")
    for {
      rsp <- Http().singleRequest(reqGetCirculationSupplys())
      body <- rsp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield body.utf8String.parseJson.convertTo[CirculationSupplys]
  }
}

object CirculationSupplyClientHttp {
  implicit val system = ActorSystem(Behaviors.empty, "CirculationSupplyClientHttp")
  implicit val ec = system.executionContext

  def apply(uri:String):CirculationSupplyClientHttp = {
    new CirculationSupplyClientHttp(uri)
  }
}
