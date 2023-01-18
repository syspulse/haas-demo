package io.syspulse.haas.ingest.eth.server

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
import io.syspulse.skel.notify.server.WebSocketServer

class AlarmSocketServer()(implicit ex:ExecutionContext,mat:ActorMaterializer) extends WebSocketServer() {
  override def process(m:Message,a:ActorRef):Message = {
    val txt = m.asTextMessage.getStrictText
    
    // debug
    log.info(s"${a} -> ${txt}")
    m
  }
}

class AlarmRoutes(uri:String)(implicit context: ActorContext[_]) extends WsRoutes(uri)(context) {
  val wss:WebSocketServer = new AlarmSocketServer()
  WS.+(wss)

  override def ws:WebSocket = wss
}