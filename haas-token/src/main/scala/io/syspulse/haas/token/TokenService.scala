package io.syspulse.haas.token

import io.jvm.uuid._

import scala.concurrent.Future
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import akka.actor.typed.ActorSystem

import com.typesafe.scalalogging.Logger

import io.syspulse.haas.token._
import io.syspulse.haas.core.serde._
import io.syspulse.haas.token.server.TokenProto
import io.syspulse.haas.token.server._
import io.syspulse.haas.core.Token

import io.syspulse.haas.token.client.TokenClientHttp

trait TokenService {
  def search(txt:String):Future[Tokens]
  def create(id:String,symbol:String,name:String):Future[Option[Token]]
}

object TokenService {
  var service:TokenService = new TokenServiceSim()
  val timeout:Timeout = Timeout(3000,TimeUnit.MILLISECONDS)

  def discover(uri:String = "")(implicit as:ActorSystem[_]):TokenService = {
    service = uri match {
      case "test://" | "" => new TokenServiceSim()
      case _ => new TokenClientHttp(uri)(as,as.executionContext)
    }
    service
  }
  
  def search(txt:String)(implicit timeout:Timeout = timeout):Tokens = {
    Await.result(service.search(txt),timeout.duration)
  }

  def create(id:String,symbol:String,name:String)(implicit timeout:Timeout = timeout):Option[Token] = {
    Await.result(service.create(id,symbol,name),timeout.duration)
  }
}


// --- For tests 
class TokenServiceSim extends TokenService {
  def search(txt:String):Future[Tokens] = Future.successful(Tokens(Seq()))

  def create(id:String,symbol:String,name:String):Future[Option[Token]] = {
    Future.successful(Some(Token(id,symbol,name)))
  }
}
