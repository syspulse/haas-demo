package io.syspulse.haas.circ

import io.jvm.uuid._

import scala.concurrent.Future
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import akka.actor.typed.ActorSystem

import com.typesafe.scalalogging.Logger

import io.syspulse.haas.circ._
import io.syspulse.haas.core.serde._
import io.syspulse.haas.circ.server.CirculationSupplyProto
import io.syspulse.haas.circ.server._
import io.syspulse.haas.circ.CirculationSupply

import io.syspulse.skel.AwaitableService
import io.syspulse.haas.circ.client.CirculationSupplyClientHttp

trait CirculationSupplyService extends AwaitableService[CirculationSupplyService] {  
  def get(id:CirculationSupply.ID):Future[Option[CirculationSupply]]
  def all():Future[CirculationSupplys]
}

object CirculationSupplyService {
  var service:CirculationSupplyService = new CirculationSupplyServiceSim()
  val timeout:Timeout = Timeout(3000,TimeUnit.MILLISECONDS)

  def discover(uri:String = "")(implicit as:ActorSystem[_]):CirculationSupplyService = {
    service = uri match {
      case "test://" | "" => new CirculationSupplyServiceSim()
      case _ => new CirculationSupplyClientHttp(uri)(as,as.executionContext)
    }
    service
  }
}


// --- For tests 
class CirculationSupplyServiceSim extends CirculationSupplyService {
  def get(id:CirculationSupply.ID):Future[Option[CirculationSupply]] = Future.successful(None)
  def all():Future[CirculationSupplys] = Future.successful(CirculationSupplys(Seq()))
}
