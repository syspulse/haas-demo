package io.syspulse.haas.intercept.store

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import com.typesafe.scalalogging.Logger

import io.jvm.uuid._

import io.syspulse.skel.Command

import io.syspulse.haas.intercept.server._
import io.syspulse.haas.intercept._
import io.syspulse.haas.intercept.script._
import io.syspulse.haas.intercept.Interception.ID
import io.syspulse.skel.util.Util
import io.syspulse.haas.intercept.Interceptor
import scala.util.Success
import scala.util.Try
import io.syspulse.skel.crypto.eth.abi.AbiStore
import org.checkerframework.checker.units.qual.A
import io.syspulse.skel.crypto.eth.abi.AbiContract
import scala.util.Failure

object InterceptionRegistry {
  val log = Logger(s"${this}")
  
  final case class GetScripts(replyTo: ActorRef[Scripts]) extends Command
  final case class GetScript(id:Script.ID,replyTo: ActorRef[Try[Script]]) extends Command

  final case class GetInterceptions(history:Option[Long],replyTo: ActorRef[Interceptions]) extends Command
  final case class GetInterception(id:ID,history:Option[Long],replyTo: ActorRef[Try[Interception]]) extends Command
  final case class FindInterceptionsByUser(uid:ID,history:Option[Long],replyTo: ActorRef[Interceptions]) extends Command
  final case class SearchInterception(txt:String,replyTo: ActorRef[Interceptions]) extends Command
  final case class GetHistory(id:Interception.ID,replyTo: ActorRef[Try[String]]) extends Command
  
  final case class GetInterceptionAbi(id:ID,aid:AbiStore.ID,replyTo: ActorRef[Try[AbiContract]]) extends Command

  
  final case class CreateInterception(interceptionCreate: InterceptionCreateReq, replyTo: ActorRef[Try[Interception]]) extends Command
  final case class CommandInterception(interceptionComman: InterceptionCommandReq, replyTo: ActorRef[InterceptionActionRes]) extends Command

  final case class DeleteInterception(id: ID, replyTo: ActorRef[InterceptionActionRes]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: InterceptionStore = null //new InterceptionStoreDB //new InterceptionStoreCache

  def apply(store: InterceptionStore,storeScript:ScriptStore, abiStore:AbiStore, interceptors:Map[String,Interceptor[_]]): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store,storeScript,abiStore,interceptors)
  }

  private def registry(store: InterceptionStore, storeScript:ScriptStore, abiStore:AbiStore, interceptors:Map[String,Interceptor[_]]): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    Behaviors.receiveMessage {
      case GetScripts(replyTo) =>
        replyTo ! Scripts(storeScript.all)
        Behaviors.same

      case GetScript(id, replyTo) =>
        replyTo ! storeScript.?(id)
        Behaviors.same

      case GetInterceptions(history,replyTo) =>
        val ixx = store.all
        val ixx1 = if(history.isDefined) 
          ixx.map(ix => ix.copy(history = ix.history.take(history.get.toInt)))
        else
          ixx
        
        replyTo ! Interceptions(ixx1)
        Behaviors.same

      case GetInterception(id, history, replyTo) =>
        val ix = store.?(id)
        val ix1 = if(history.isDefined && ix.isSuccess) 
          ix.map(ix => ix.copy(history = ix.history.take(history.get.toInt)))
        else 
          ix
        replyTo ! ix1
        Behaviors.same
      
      case GetHistory(id, replyTo) =>
        val ix = store.?(id)
        val csv = ix.map(ix => {
          ix.history.foldLeft("")( (o,h) => o + Util.toCSV(h) + "\n")
        })
        replyTo ! csv
        Behaviors.same      

      case FindInterceptionsByUser(uid, history, replyTo) =>
        val ixx = store.findByUser(uid)
        val ixx1 = if(history.isDefined) 
          ixx.map(ix => ix.copy(history = ix.history.take(history.get.toInt)))
        else
          ixx
        replyTo ! Interceptions(ixx1)
        Behaviors.same
      
      case SearchInterception(txt, replyTo) =>
        replyTo ! Interceptions(store.search(txt))
        Behaviors.same
      
     
      case CreateInterception(c, replyTo) =>
        log.info(s"${c}")
        for {
          script <- {
            // special case to reference script body
            if(c.script.trim.startsWith("ref://")) {
              val scriptId = c.script.trim.stripPrefix("ref://")
              storeScript.?(scriptId) match {
                case Success(s) => Some(s)
                case Failure(e) => 
                  log.error(s"script not found: ${scriptId}")
                  replyTo ! Failure(e)
                  None                  
              }
            } else {
              val scriptId = Util.sha256(c.script)
              val script = Script(scriptId,"js",c.script,c.name,System.currentTimeMillis,uid = c.uid)
              storeScript.+(script)
              Some(script)
            }
          }
          entity <- {
            Some(c.entity.getOrElse("tx"))
          }
          abiId <- { entity match {
            case "event" | "function" | "func" => 
              if(c.abi.isDefined && c.contract.isDefined) {
                val abiId = c.contract.get
                val ac = AbiContract(abiId,c.abi.get,Some(System.currentTimeMillis))
                
                log.info(s"${ac}")
                val r = abiStore.+(ac)
                
                if(r.isFailure) {
                  log.error(s"ABI not decoded: ${abiId}: ${r}")
                  replyTo ! Failure(new Exception(s"ABI not decoded: ${abiId}: ${r}"))
                  None
                } else {
                  log.info(s"abiStore: ${abiStore.size}")
                  Some(abiId)
                }
              } else {
                log.error(s"ABI or Contract not found: ${entity}")
                replyTo ! Failure(new Exception(s"ABI or Contract not found: ${entity}"))
                None
              }
            case _ => 
              Some("")
          }}
          ix <- {
            val ix = Interception(c.id.getOrElse(UUID.random), c.name, script.id, c.alarm, c.uid, entity, if(abiId.isBlank) None else Some(abiId))
            log.info(s"${ix}")
            Some(ix)
          }
          store1 <- interceptors.get(entity) match {
            case Some(interceptor) => 
              val store1 = store.+(ix)
              interceptor.+(ix)
              replyTo ! Success(ix)
              Some(store1)
            case None => 
              log.warn(s"Interceptor not found: entity='${entity}'")
              replyTo ! Failure(new Exception(s"Interceptor not found: entity='${entity}'"))
              None
          }
                  
        } yield store1
          
        Behaviors.same
                
        // val script = 
        //   // special case to reference script body
        //   if(c.script.trim.startsWith("ref://")) {
        //     val scriptId = c.script.trim.stripPrefix("ref://")
        //     storeScript.?(scriptId) match {
        //       case Success(s) => s
        //       case Failure(e) => 
        //         log.error(s"script not found: ${scriptId}")
        //         replyTo ! Failure(e)
        //         return Behaviors.same
        //     }
        //   } else {
        //     val scriptId = Util.sha256(c.script)
        //     val script = Script(scriptId,"js",c.script,c.name)
        //     storeScript.+(script)
        //     script
        //   }
        
        // //val scriptId = Util.sha256(c.script)
        // //val script = Script(scriptId,"js",src,c.name)
        // //storeScript.+(script)

        // val entity = c.entity.getOrElse("tx")

        // val abiId = entity match {          
        //   case "event" | "function" => 
        //     if(c.abi.isDefined && c.contract.isDefined) {
        //       val abiId = c.contract.get
        //       abiStore.+(AbiContract(abiId,c.abi.get,Some(System.currentTimeMillis)))
        //       Some(abiId)
        //     } else {
        //       log.error(s"ABI or Contract not found: ${entity}")
        //       replyTo ! Failure(new Exception(s"ABI or Contract not found: ${entity}"))
        //       return Behaviors.same
        //     }
        //   case _ => 
        //     None
        // }

        // val ix = Interception(c.id.getOrElse(UUID.random), c.name, script.id, c.alarm, c.uid, entity, abiId)
        
        // val store1 = interceptors.get(entity) match {
        //   case Some(interceptor) => 
        //     val store1 = store.+(ix)
        //     interceptor.+(ix)
        //     replyTo ! Success(ix)
        //     store1
        //   case None => 
        //     log.warn(s"Interceptor not found: entity='${entity}'")
        //     replyTo ! Failure(new Exception(s"Interceptor not found: entity='${entity}'"))
        //     Success(store)
        // }

        // //replyTo ! Success(ix)
        // registry(store1.getOrElse(store),storeScript,abiStore,interceptors)

      case CommandInterception(c, replyTo) =>
        val ix:Option[Interception] = c.id.flatMap(id => store.?(id).toOption)

        if(! ix.isDefined) {
          log.warn(s"Interceptor not found: id='${c.id}'")
          replyTo ! InterceptionActionRes("not found",c.id.map(_.toString))  
        } else {
          val id = ix.get.id
          val status = c.command match {
            case "start" => 
              val st = store.start(id)
              interceptors.values.foreach(_.start(id))
              st.toString
              
            case "stop" => 
              val st = store.stop(id)
              interceptors.values.foreach(_.stop(id))
              st.toString

            case _ => "unknown"
          }
          
          replyTo ! InterceptionActionRes(status,Some(id.toString))
        }
        Behaviors.same
      
      case DeleteInterception(id, replyTo) =>
        val store1 = store.del(id)

        interceptors.values.foreach(intx => intx.-(id))

        replyTo ! InterceptionActionRes(s"Success",Some(id.toString))
        
        registry(store1.getOrElse(store),storeScript,abiStore,interceptors)

      case GetInterceptionAbi(id,aid, replyTo) => 
        val r = abiStore.?(aid)
        replyTo ! r

        Behaviors.same
    }
  }
}
