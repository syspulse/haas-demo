package io.syspulse.haas.ingest.eth.alarm

import com.typesafe.scalalogging.Logger

import java.time.ZonedDateTime
import scala.util.Try
import scala.util.Success
import scala.collection.immutable.Queue

import io.syspulse.skel.notify.Notification
import io.syspulse.skel.notify.Config

import io.syspulse.haas.ingest.eth.intercept.Interception
import io.syspulse.skel.config._

case class UserAlarm(scriptId:String,to:Seq[String])

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
  var queue = Queue[Interception]()

  // TODO: change to Akka Stream
  new Thread(){
    override def run(): Unit = {
      while( true ) {
        Thread.sleep(throttle)
        //log.info(s"${Console.GREEN}ALARM${Console.RESET} -> ${queue}")

        // group by scriptId
        val queueScriptId = queue.groupBy(_.scriptId)
        
        queueScriptId.foreach{ case(scriptId,intercepts) => {
          
          Alarms.userAlarms.get(scriptId) match {
            case Some(userAlarms) => {
              // create all UserAlarms distanations without duplicates
              val allTo = userAlarms.map(_.to).flatten.distinct
              val allNotify = Notification.parseUri(allTo)._1

              // combile one message from all Interceptions into one
              val txt = intercepts.foldLeft("")((s,ix) => s + s"Interception: ${ix}\n" )

              // broadcast to all Notifiers              
              Notification.broadcast(allNotify.receviers,s"Alarms",s"${Console.GREEN}Interception for ${scriptId}:\n${Console.YELLOW}${txt}${Console.RESET}")
            }
            case _ =>
          }          
        }}
        // clear the queue
        queue = Queue()
      }
    }  
  }.start()

  def send(ix:Interception):Alarms = {
    // filter only to where there is UserAlarm associtated
    queue = queue :+ ix
    this
  }
}

object Alarms {
  var userAlarms = Map[String,List[UserAlarm]]()

  // format: scriptId:uri;uri
  // scriptId is currently full path to scipt file with SCRIPT- prefix
  // exmaple: SCRIPT-file://scripts/script-1.js=email://user@mail.com;stdout://
  def +(scriptId:String,alarmUri:Seq[String]):Unit = {
    val userAlarmUri = alarmUri.groupBy(a => a.split("=").toList match {
      case sid :: ua :: Nil => sid
      case ua :: Nil => ""
      case _ => ""
    })
    
    val aa = userAlarmUri.get(scriptId).getOrElse(userAlarmUri.get("").getOrElse(Seq())) 
    aa.foreach(a => {
      val uri = a.split("=").toList match {
        case _ :: ua :: Nil => ua
        case ua :: Nil => ua
        case _ => "stdout://"    
      }
      
      Alarms.+(UserAlarm(scriptId,uri.split(";").toSeq))
    })

    
  }

  def +(ua:UserAlarm):Unit = {
    userAlarms = userAlarms + (ua.scriptId -> (userAlarms.get(ua.scriptId).getOrElse(List()) :+ ua))
  }
}