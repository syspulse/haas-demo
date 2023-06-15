package io.syspulse.haas.intercept.server

import scala.util.{Try,Success,Failure}
import com.typesafe.scalalogging.Logger

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext

import akka.http.scaladsl.model.ws.Message
import akka.stream.ActorMaterializer
import akka.actor.ActorRef
import scala.concurrent.ExecutionContext

import io.syspulse.skel.service.ws.WsRoutes
import io.syspulse.skel.service.ws.WebSocket
import io.syspulse.skel.notify.server.WS

class AlarmSocketServer()(implicit ex:ExecutionContext,mat:ActorMaterializer) extends WebSocket(idleTimeout = 1000L*60*60*24) {
  override def process(m:Message,a:ActorRef):Message = {
    val txt = m.asTextMessage.getStrictText
    
    // debug
    log.debug(s"${a} -> ${txt}")
    m
  }
}

class AlarmRoutes(uri:String)(implicit context: ActorContext[_]) extends WsRoutes(uri)(context) {
  val wss = new AlarmSocketServer()
  
  override def ws:WebSocket = wss
}
