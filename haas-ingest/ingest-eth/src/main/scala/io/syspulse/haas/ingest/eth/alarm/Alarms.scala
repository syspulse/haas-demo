package io.syspulse.haas.ingest.eth.alarm

import com.typesafe.scalalogging.Logger

import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success
import scala.collection.immutable.Queue

import io.syspulse.skel.notify.Notification
import io.syspulse.skel.notify.Config

import io.syspulse.haas.ingest.eth.intercept.Interception

case class UserAlarm(scriptId:String,to:Seq[String])

class Alarms(throttle:Long = 10000L) {
  protected val log = Logger(s"${this.getClass()}")

  implicit val config = io.syspulse.skel.notify.Config()

  @volatile
  var queue = Queue[Interception]()

  // TODO: change to Akka Stream
  new Thread(){
    override def run(): Unit = {
      while( true ) {
        Thread.sleep(throttle)
        //log.info(s"${Console.GREEN}ALARM${Console.RESET} -> ${queue}")
        
        queue.foreach( ix => {
          val scriptId = ix.scriptId
          val userAlarms = Alarms.userAlarms.get(scriptId)

          userAlarms match {
            case Some(uaa) => {
              val nrr = uaa.map(ua => Notification.parseUri(ua.to.toList)).map(_._1.receviers).flatten
              Notification.broadcast(nrr,s"Alarms for ${scriptId}",s"Interception:\n\n${ix}")
            }
            case _ =>
          }          
        })        
        // clear the queue
        queue = Queue()
      }
    }  
  }.start()

  def send(ix:Interception):Alarms = {    
    queue = queue :+ ix
    this
  }
}

object Alarms {
  var userAlarms = Map[String,List[UserAlarm]]()

  def +(ua:UserAlarm) = {
    userAlarms = userAlarms + (ua.scriptId -> (userAlarms.get(ua.scriptId).getOrElse(List()) :+ ua))
  }
}