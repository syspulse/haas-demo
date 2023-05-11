package io.syspulse.haas.supply.client

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

import io.syspulse.haas.supply._
import io.syspulse.haas.supply.server.SupplyProto
import io.syspulse.haas.supply.server._
import io.syspulse.haas.supply.Supply

class SupplyClientHttp(uri:String)(implicit as:ActorSystem[_], ec:ExecutionContext) extends ClientHttp(uri)(as,ec) with SupplyService {
  import spray.json._
  import SupplyJson._
  import SupplyProto._  
  
  def reqGetSupplySupply(id:Supply.ID) = HttpRequest(method = HttpMethods.GET, uri = s"${uri}/${id}")  
  def reqGetSupplySupplys() = HttpRequest(method = HttpMethods.GET, uri = s"${uri}")
  
  def get(id:Supply.ID):Future[Option[Supply]] = {
    log.info(s"${id} -> ${reqGetSupplySupply(id)}")
    for {
      rsp <- Http().singleRequest(reqGetSupplySupply(id))
      supply <- if(rsp.status == StatusCodes.OK) Unmarshal(rsp).to[Option[Supply]] else Future(None)
    } yield supply 
  }
  def all():Future[Supplys] = {
    log.info(s" -> ${reqGetSupplySupplys()}")
    for {
      rsp <- Http().singleRequest(reqGetSupplySupplys())
      body <- rsp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield body.utf8String.parseJson.convertTo[Supplys]
  }
}

object SupplyClientHttp {
  implicit val system = ActorSystem(Behaviors.empty, "SupplyClientHttp")
  implicit val ec = system.executionContext

  def apply(uri:String):SupplyService = {
    new SupplyClientHttp(uri)
  }
}
