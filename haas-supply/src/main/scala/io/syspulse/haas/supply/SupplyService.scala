package io.syspulse.haas.supply

import io.jvm.uuid._

import scala.concurrent.Future
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.typed.ActorSystem

import com.typesafe.scalalogging.Logger

import io.syspulse.haas.supply._
import io.syspulse.haas.serde._
import io.syspulse.haas.supply.server.SupplyProto
import io.syspulse.haas.supply.server._
import io.syspulse.haas.supply.Supply

import io.syspulse.skel.ExternalService
import io.syspulse.haas.supply.client.SupplyClientHttp
import scala.concurrent.duration.FiniteDuration

trait SupplyService extends ExternalService[SupplyService] {  
  def get(id:Supply.ID):Future[Option[Supply]]
  def all():Future[Supplys]
}

object SupplyService {
  var service:SupplyService = new SupplyServiceSim()
  val timeout:Timeout = Timeout(3000,TimeUnit.MILLISECONDS)

  def discover(uri:String = "")(implicit as:ActorSystem[_]):SupplyService = {
    service = uri match {
      case "test://" | "" => new SupplyServiceSim()
      case _ => new SupplyClientHttp(uri)(as,as.executionContext)
    }
    service
  }
}


// --- For tests 
class SupplyServiceSim extends SupplyService {
  def get(id:Supply.ID):Future[Option[Supply]] = Future.successful(None)
  def all():Future[Supplys] = Future.successful(Supplys(Seq()))

  def withAccessToken(token:String):SupplyServiceSim = this
  def withTimeout(timeout:FiniteDuration = FiniteDuration(1000, TimeUnit.MILLISECONDS)):SupplyServiceSim = this
}
