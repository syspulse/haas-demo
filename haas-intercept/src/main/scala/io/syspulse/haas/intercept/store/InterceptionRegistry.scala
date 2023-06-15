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
import io.syspulse.haas.core.Blockchain

object InterceptionRegistry {
  val log = Logger(s"${this}")
  
  final case class GetScripts(replyTo: ActorRef[Scripts]) extends Command
  final case class GetScript(id:Script.ID,replyTo: ActorRef[Try[Script]]) extends Command
  final case class UpdateScript(id:Script.ID, uid:Option[UUID], scriptUpdate: ScriptUpdateReq, replyTo: ActorRef[Try[Script]]) extends Command
  final case class DelScript(id:Script.ID,replyTo: ActorRef[Try[ActionRes]]) extends Command

  final case class GetInterceptions(history:Option[Long],replyTo: ActorRef[Interceptions]) extends Command
  final case class GetInterception(id:ID,history:Option[Long],replyTo: ActorRef[Try[Interception]]) extends Command
  final case class FindInterceptionsByUser(uid:ID,history:Option[Long],replyTo: ActorRef[Interceptions]) extends Command
  final case class SearchInterception(txt:String,replyTo: ActorRef[Interceptions]) extends Command
  final case class GetHistory(id:Interception.ID,replyTo: ActorRef[Try[String]]) extends Command
  
  final case class GetInterceptionAbi(id:ID,aid:AbiStore.ID,replyTo: ActorRef[Try[AbiContract]]) extends Command

  
  final case class CreateInterception(req: InterceptionCreateReq, replyTo: ActorRef[Try[Interception]]) extends Command
  final case class CommandInterception(interceptionComman: InterceptionCommandReq, replyTo: ActorRef[ActionRes]) extends Command
  final case class DeleteInterception(id: ID, replyTo: ActorRef[ActionRes]) extends Command
  final case class UpdateInterception(id: ID, req: InterceptionUpdateReq, replyTo: ActorRef[Try[Interception]]) extends Command
  
  // this var reference is unfortunately needed for Metrics access
  var store: InterceptionStore = null //new InterceptionStoreDB //new InterceptionStoreCache

  def apply(store: InterceptionStore,scriptStore:ScriptStore, abiStore:AbiStore, interceptors:Map[String,Interceptor[_]])(implicit config:Config): Behavior[io.syspulse.skel.Command] = {
    this.store = store
    registry(store,scriptStore,abiStore,interceptors)(config)
  }

  private def registry(store: InterceptionStore, scriptStore:ScriptStore, abiStore:AbiStore, interceptors:Map[String,Interceptor[_]])(config:Config): Behavior[io.syspulse.skel.Command] = {
    this.store = store

    def create(c:InterceptionCreateReq):Try[Interception] = {
      log.info(s"${c}")
      for {
        script <- {
          // special case to reference script body
          if(c.script.trim.startsWith("ref://")) {
            val scriptId = c.script.trim.stripPrefix("ref://")
            scriptStore.?(scriptId) match {
              case Failure(e) => 
                log.error(s"script not found: ${scriptId}")
                //replyTo ! Failure(e)
                Failure(e)
              case s => s
            }
          } else {
            // generate scirptId unique per user
            val scriptId = c.uid.getOrElse(UUID(Array.fill[Byte](16)(0))).toString.replaceAll("-","") + "-" + Util.sha256(c.script).take(16)
            val script = Script(scriptId,"js",c.script,c.name,System.currentTimeMillis,uid = c.uid)
            scriptStore.+(script)
            
            Success(script)
          }
        }
        entity <- {
          Success(c.entity.getOrElse("tx"))            
        }
        bid <- {
          Success(c.bid.getOrElse(Blockchain.ETHEREUM_MAINNET).toLowerCase)
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
                //replyTo ! Failure(new Exception(s"ABI not decoded: ${abiId}: ${r}"))
                Failure(new Exception(s"ABI not decoded: ${abiId}: ${r}"))
              } else {
                log.info(s"abiStore: ${abiStore.size}")
                Success(abiId)
              }
            } else {
              log.error(s"ABI or Contract not found: ${entity}")
              //replyTo ! Failure(new Exception(s"ABI or Contract not found: ${entity}"))
              Failure(new Exception(s"ABI or Contract not found: ${entity}"))
            }
          case _ => 
            Success("")
        }}
        ix <- {
          val ix = Interception(
            c.id.getOrElse(UUID.random), 
            c.name, 
            script.id, 
            c.alarm, 
            c.uid, 
            entity, 
            if(abiId.isBlank) None else Some(abiId),
            bid = Some(bid),
            limit = c.limit.orElse(Some(config.history)),
            status = c.status.getOrElse(Interception.STARTED)
          )
          
          log.info(s"${ix}")
          Success(ix)
        }
        ix2 <- {
          val ixLocator = s"${bid}.${entity}"
          interceptors.get(ixLocator) match {
            case Some(interceptor) => 
              val store1 = store.+(ix)
              interceptor.+(ix)
              //replyTo ! Success(ix)
              Success(ix)
            case None => 
              log.warn(s"Interceptor not found: '${ixLocator}'")
              //replyTo ! Failure(new Exception(s"Interceptor not found: '${ixLocator}'"))
              Failure(new Exception(s"Interceptor not found: '${ixLocator}'"))
          }
        }
                
      } yield ix2      
    }

    Behaviors.receive { (ctx,msg) => msg match {
      case GetScripts(replyTo) =>
        replyTo ! Scripts(scriptStore.all)
        Behaviors.same

      case GetScript(id, replyTo) =>
        replyTo ! scriptStore.?(id)
        Behaviors.same

      case DelScript(id, replyTo) =>
        val r = scriptStore.del(id).map(_ => ActionRes("deleted",Some(id)))
        replyTo ! r
        Behaviors.same

      case UpdateScript(id, uid, req, replyTo) =>
        // WARNING: ignore UserId for now
        // WARNING: changing script must also trigger recompile on-demand !
        replyTo ! scriptStore.update(id, req.name,req.desc,req.src)
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
      
      case UpdateInterception(id,u,replyTo) =>
        log.info(s"update: ${id}: ${u}")
        // very heavy and inefficient
        val ix2 = for {
          ix1 <- store.?(id)
          script <- scriptStore.?(ix1.scriptId)
          abiJson <- if(ix1.aid.isDefined) abiStore.?(ix1.aid.get).map( a => Some(a.json)) else Success(None)
          ix2 <- {
            val req = InterceptionCreateReq(
                id = Some(id),
                name = u.name.getOrElse(ix1.name),
                script = u.script.getOrElse(script.src),
                alarm = u.alarm.getOrElse(ix1.alarm), 
                uid = (if(u.uid.isDefined) u.uid else ix1.uid),
                bid = (if(u.bid.isDefined) u.bid else ix1.bid),
                entity = (if(u.entity.isDefined) u.entity else Some(ix1.entity)),
                abi = (if(u.abi.isDefined) u.abi else abiJson),
                contract = u.contract)

            create( req )
          }
        } yield ix2
        
        replyTo ! ix2
        Behaviors.same

      case CreateInterception(c, replyTo) =>
        val ix = create(c)
        replyTo ! ix
        Behaviors.same                      

      case CommandInterception(c, replyTo) =>
        val ix:Option[Interception] = c.id.flatMap(id => store.?(id).toOption)

        if(! ix.isDefined) {
          log.warn(s"Interceptor not found: id='${c.id}'")
          replyTo ! ActionRes("not found",c.id.map(_.toString))  
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
          
          replyTo ! ActionRes(status,Some(id.toString))
        }
        Behaviors.same
      
      case DeleteInterception(id, replyTo) =>
        val store1 = store.del(id)

        interceptors.values.foreach(intx => intx.-(id))

        replyTo ! ActionRes(s"Success",Some(id.toString))
        
        Behaviors.same

      case GetInterceptionAbi(id,aid, replyTo) => 
        val r = abiStore.?(aid)
        replyTo ! r

        Behaviors.same
    }}
  }
}
