package io.syspulse.haas.ingest.eth.alarm

import com.typesafe.scalalogging.Logger
import io.jvm.uuid._

import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success
import scala.collection.immutable.Queue

import io.syspulse.skel.notify.Notification
import io.syspulse.skel.notify.Config

import io.syspulse.haas.ingest.eth.intercept.Interception
import io.syspulse.haas.ingest.eth.intercept.InterceptionAlarm
import io.syspulse.skel.config._

//case class UserAlarm(alarmId:String,to:Seq[String])

class Alarms(throttle:Long = 10000L) {
  protected val log = Logger(s"${this.getClass()}")

  val c = Configuration.withPriority(Seq(new ConfigurationAkka,new ConfigurationProp,new ConfigurationEnv))
  val d = io.syspulse.skel.notify.Config()
  implicit val config = io.syspulse.skel.notify.Config(
    smtpUri = c.getString("smtp.uri").getOrElse(Configuration.withEnv(d.smtpUri)),
    smtpFrom = c.getString("smtp.from").getOrElse(d.smtpFrom),
    snsUri = c.getString("sns.uri").getOrElse(Configuration.withEnv(d.snsUri)),
    telegramUri = c.getString("telegram.uri").getOrElse(Configuration.withEnv(d.telegramUri)),
  )

  @volatile
  var queue = Queue[InterceptionAlarm]()

  // TODO: change to Akka Stream
  new Thread(){
    override def run(): Unit = {
      import spray.json._
      import DefaultJsonProtocol._
      import io.syspulse.haas.ingest.eth.intercept.InterceptionJson._

      while( true ) {
        Thread.sleep(throttle)
        
        // group by alarmId
        val queueById = queue.groupBy(_.iid)
        
        queueById.foreach{ case(iid,ixs) => {
          
          val allTo = ixs.map(_.alarm).flatten.distinct.toList
          val allNotify = Notification.parseUri(allTo)._1

          val txt = 
            //ixs.foldLeft("")((s,ia) => s + s"InterceptionAlarm: ${ia}\n" )
            ixs.foldLeft("")((s,ia) => s + s"${ia.toJson}\n" )
          val msg = //s"${Console.GREEN}InterceptionAlarm for ${iid}:\n${Console.YELLOW}${txt}${Console.RESET}"
            txt
          val title = ""//s"Alarms"

          Notification.broadcast(allNotify.receviers,title,msg)      
        }}
        // clear the queue
        queue = Queue()
      }
    }  
  }.start()

  def send(ix:InterceptionAlarm):Alarms = {
    // filter only to where there is UserAlarm associtated
    queue = queue :+ ix
    this
  }
}
