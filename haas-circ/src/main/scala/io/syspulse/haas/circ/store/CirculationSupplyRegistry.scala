package io.syspulse.haas.circ.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.skel.Command
import io.syspulse.skel.job.{JobNotification,JobNotificationJson}

import io.syspulse.skel.syslog.{SyslogEvent,SyslogBus}
import io.syspulse.haas.circ.server._
import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation
import io.syspulse.haas.circ.Config


object CirculationSupplyRegistry {
  val log = Logger(s"${this}")
    
  final case class GetCirculationSupplys(replyTo: ActorRef[CirculationSupplys]) extends Command
  final case class GetCirculationSupply(id:CirculationSupply.ID,ts0:Long,ts1:Long,replyTo: ActorRef[Option[CirculationSupply]]) extends Command
  final case class GetCirculationSupplyByToken(tid:String,ts0:Long,ts1:Long,replyTo: ActorRef[Option[CirculationSupply]]) extends Command
  final case class GetCirculationSupplyLast(tokens:Seq[String],from:Int,size:Int,replyTo: ActorRef[CirculationSupplys]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: CirculationSupplyStore = null //new CirculationSupplyStoreDB //new CirculationSupplyStoreCache

  def apply(store: CirculationSupplyStore = new CirculationSupplyStoreMem)(implicit config:Config): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    val syslog = new SyslogBus("circ",config.syslogUri,config.syslogChannel) {
      import spray.json._
      import JobNotificationJson._

      val ts0 = System.currentTimeMillis

      override def recv(ev:SyslogEvent):SyslogEvent = {
        log.info(s"event=${ev}")
        if(ts0 < ev.ts) {
          // ignore old events if missed
          try {
            val job = ev.msg.parseJson.convertTo[JobNotification]
            log.info(s"job=${job}")
            if(job.src.contains("circ-holders-supply")) {
              store.clear()
              store.reload()
            } else 
              log.warn(s"script: ${job.src}: ignoring")
            
          } catch {
            case e:Exception => 
              log.warn(s"expected JobNotification",e)
          }        
        }
        ev
      }
    }.withScope("sys.job")
  
    registry(store)
  }

  private def registry(store: CirculationSupplyStore): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    Behaviors.receiveMessage {
      case GetCirculationSupplys(replyTo) =>
        replyTo ! CirculationSupplys(store.all)
        Behaviors.same

      case GetCirculationSupply(id,ts0,ts1,replyTo) =>
        replyTo ! store.?(id,ts0,ts1)
        Behaviors.same

      case GetCirculationSupplyByToken(tid,ts0,ts1,replyTo) =>
        replyTo ! store.findByToken(tid,ts0,ts1)
        Behaviors.same

      case GetCirculationSupplyLast(tokens,from,size,replyTo) =>
        replyTo ! CirculationSupplys(store.lastByTokens(tokens,from,size))
        Behaviors.same      
    }
  }
}
