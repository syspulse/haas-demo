package io.syspulse.haas.token.client

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

import io.syspulse.haas.token._
import io.syspulse.haas.core.serde._
import io.syspulse.haas.token.server.TokenProto
import io.syspulse.haas.token.server._
import io.syspulse.haas.core.Token

class TokenClientHttp(uri:String)(implicit as:ActorSystem[_], ec:ExecutionContext) extends ClientHttp(uri)(as,ec) with TokenService {
  
  import TokenJson._
  import TokenProto._
  import spray.json._
  
  def reqGetToken(id:Token.ID) = HttpRequest(method = HttpMethods.GET, uri = s"${uri}/${id}")  
  def reqGetTokens() = HttpRequest(method = HttpMethods.GET, uri = s"${uri}")
  def reqPostToken(id:String,symbol:String,name:String) =  HttpRequest(method = HttpMethods.POST, uri = s"${uri}",
        entity = HttpEntity(ContentTypes.`application/json`, 
          TokenCreateReq(id,symbol,name).toJson.toString)
      )
  def reqDeleteToken(id:Token.ID) = HttpRequest(method = HttpMethods.DELETE, uri = s"${uri}/${id}")

  def reqSearchToken(txt:String) = HttpRequest(method = HttpMethods.GET, uri = s"${uri}/search/${txt}")
  def reqTypingToken(txt:String) = HttpRequest(method = HttpMethods.GET, uri = s"${uri}/typing/${txt}")

  def delete(id:Token.ID):Future[TokenActionRes] = {
    log.info(s"${id} -> ${reqDeleteToken(id)}")
    for {
      rsp <- Http().singleRequest(reqDeleteToken(id))
      r <- if(rsp.status == StatusCodes.OK) Unmarshal(rsp).to[TokenActionRes] else Future(TokenActionRes(status=s"${rsp.status}: ${rsp.entity}",None))
    } yield r 
  }

  def create(id:String,symbol:String,name:String):Future[Option[Token]] = {
    log.info(s"-> ${reqPostToken(id,symbol,name)}")
    for {
      rsp <- Http().singleRequest(reqPostToken(id,symbol,name))
      r <- if(rsp.status == StatusCodes.OK) Unmarshal(rsp).to[Option[Token]] else Future(None)
    } yield r
  }

  def get(id:Token.ID):Future[Option[Token]] = {
    log.info(s"${id} -> ${reqGetToken(id)}")
    for {
      rsp <- Http().singleRequest(reqGetToken(id))
      token <- if(rsp.status == StatusCodes.OK) Unmarshal(rsp).to[Option[Token]] else Future(None)
    } yield token 
  }

  def search(txt:String):Future[Tokens] = {
    log.info(s"${txt} -> ${reqSearchToken(txt)}")
    for {
      rsp <- Http().singleRequest(reqSearchToken(txt))        
      tokens <- if(rsp.status == StatusCodes.OK) Unmarshal(rsp).to[Tokens] else Future(Tokens(Seq()))
    } yield tokens
  }

  def typing(txt:String):Future[Tokens] = {
    log.info(s"${txt} -> ${reqTypingToken(txt)}")
    for {
      rsp <- Http().singleRequest(reqTypingToken(txt))        
      tokens <- if(rsp.status == StatusCodes.OK) Unmarshal(rsp).to[Tokens] else Future(Tokens(Seq()))
    } yield tokens
  }

  def all():Future[Tokens] = {
    log.info(s" -> ${reqGetTokens()}")
    for {
      rsp <- Http().singleRequest(reqGetTokens())
      body <- rsp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield body.utf8String.parseJson.convertTo[Tokens]
  }
}

object TokenClientHttp {
  implicit val system = ActorSystem(Behaviors.empty, "TokenClientHttp")
  implicit val ec = system.executionContext

  def apply(uri:String):TokenClientHttp = {
    new TokenClientHttp(uri)
  }
}
