package io.syspulse.haas.intercept.alarm

import com.typesafe.scalalogging.Logger
import io.jvm.uuid._

import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success
import scala.collection.immutable.Queue

import io.syspulse.skel.config._
import io.syspulse.skel.notify.Notification
import io.syspulse.skel.notify.Config

import io.syspulse.haas.intercept.Interception
import io.syspulse.haas.intercept.InterceptionAlarm
import io.syspulse.skel.notify.NotifySeverity


class Alarms(throttle:Long = 10000L, interceptions:Map[Interception.ID,Interception]) {
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
      import io.syspulse.haas.intercept.InterceptionJson._

      while( true ) {
        Thread.sleep(throttle)
        
        // group by alarmId
        val queueById = queue.groupBy(_.iid)
        
        queueById.foreach{ case(iid,iaa) => {
          
          val allTo = iaa.map(_.alarm).flatten.distinct.toList

          // don't send alarms for none://          
          if(! (allTo.size == 1 && allTo.head == "none://")) {
            val allNotify = Notification.parseUri(allTo)._1

            val txt = iaa.foldLeft("")((s,ia) => s + s"${ia.toJson}\n" )
            
            val ix = interceptions.get(iid)
            val msg = txt
            val title = s"${ix.map(ix => ix.name).getOrElse("")}: ${ix.map(_.history.size)} (${iid})"

            Notification.broadcast(allNotify.receviers, title, msg, Some(NotifySeverity.INFO), Some("user.intercept"))
          }
        }}
        // clear the queue
        queue = Queue()
      }
    }  
  }.start()

  def send(ia:InterceptionAlarm):Alarms = {
    // filter only to where there is UserAlarm associtated

    // LIFO 
    queue = queue.prepended(ia)
    this
  }
}
