package io.syspulse.haas.intercept.server

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.model.StatusCodes._
import com.typesafe.scalalogging.Logger

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import io.jvm.uuid._

import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import jakarta.ws.rs.{Consumes, POST, GET, DELETE, Path, Produces}
import jakarta.ws.rs.core.MediaType

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter

import io.syspulse.skel.service.Routeable
import io.syspulse.skel.service.CommonRoutes

import io.syspulse.skel.Command

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import io.syspulse.skel.auth.permissions.rbac.Permissions
import io.syspulse.skel.auth.RouteAuthorizers

import io.syspulse.haas.intercept.store.InterceptionRegistry
import io.syspulse.haas.intercept.store.InterceptionRegistry._
import io.syspulse.haas.intercept._
import io.syspulse.haas.intercept.script._

import io.syspulse.haas.intercept.script.ScriptJson
import scala.util.Try
import akka.http.scaladsl.model.headers.RawHeader
import io.syspulse.skel.crypto.eth.abi.AbiContract
import io.syspulse.skel.crypto.eth.abi.AbiStore
import io.syspulse.skel.crypto.eth.abi.AbiContractJson

@Path("/")
class InterceptionRoutes(registry: ActorRef[Command])(implicit context: ActorContext[_]) extends CommonRoutes with Routeable with RouteAuthorizers {
  //val log = Logger(s"${this}")
  implicit val system: ActorSystem[_] = context.system
  
  implicit val permissions = Permissions()

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import ScriptJson._
  import InterceptionJson._
  import InterceptionProto._
  import AbiContractJson._
  
  // registry is needed because Unit-tests with multiple Routes in Suites will fail (Prometheus libary quirk)
  val cr = new CollectorRegistry(true);
  val metricGetCount: Counter = Counter.build().name("skel_intercept_get_total").help("Interception gets").register(cr)
  val metricDeleteCount: Counter = Counter.build().name("skel_intercept_delete_total").help("Interception deletes").register(cr)
  val metricCreateCount: Counter = Counter.build().name("skel_intercept_create_total").help("Interception creates").register(cr)
  
  def getScripts(): Future[Scripts] = registry.ask(GetScripts)
  def getScript(id: Script.ID): Future[Try[Script]] = registry.ask(GetScript(id, _))

  def getInterceptions(history:Option[Long]): Future[Interceptions] = registry.ask(GetInterceptions(history, _))
  def getInterception(id: Interception.ID,history:Option[Long]): Future[Try[Interception]] = registry.ask(GetInterception(id,history, _))
  def findInterceptionsByUser(uid: UUID,history:Option[Long]): Future[Interceptions] = registry.ask(FindInterceptionsByUser(uid, history, _))
  def getInterceptionBySearch(txt: String): Future[Interceptions] = registry.ask(SearchInterception(txt, _))
  def getHistory(id: Interception.ID): Future[Try[String]] = registry.ask(GetHistory(id, _))
  
  def getInterceptionAbi(id: Interception.ID,aid:AbiStore.ID): Future[Try[AbiContract]] = registry.ask(GetInterceptionAbi(id,aid, _))
  
  def createInterception(interceptCreate: InterceptionCreateReq): Future[Try[Interception]] = registry.ask(CreateInterception(interceptCreate, _))
  def deleteInterception(id: Interception.ID): Future[InterceptionActionRes] = registry.ask(DeleteInterception(id, _))
  def commandInterception(interceptCommand: InterceptionCommandReq): Future[InterceptionActionRes] = registry.ask(CommandInterception(interceptCommand, _))

  @GET @Path("/{id}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Return Interception by id",
    parameters = Array(new Parameter(name = "id", in = ParameterIn.PATH, description = "Interception id")),
    responses = Array(new ApiResponse(responseCode="200",description = "Interception returned",content=Array(new Content(schema=new Schema(implementation = classOf[Interception])))))
  )
  def getInterceptionRoute(id: String) = get {
    rejectEmptyResponse {
      parameters("history".as[Long].optional) { (history) => 
        onSuccess(getInterception(UUID(id),history)) { r =>
          metricGetCount.inc()
          encodeResponse { complete(r) }
        }
      }
    }
  }

  @GET @Path("/user/{uid}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Find Interception by uid",
    parameters = Array(new Parameter(name = "uid", in = ParameterIn.PATH, description = "User id")),
    responses = Array(new ApiResponse(responseCode="200",description = "Interceptions",content=Array(new Content(schema=new Schema(implementation = classOf[Interception])))))
  )
  def getInterceptionsFindByUserRoute(uid: String) = get {
    rejectEmptyResponse {
      parameters("history".as[Long].optional) { (history) => 
        onSuccess(findInterceptionsByUser(UUID(uid),history)) { r =>
          metricGetCount.inc()
          encodeResponse { complete(r) }
        }
      }
    }
  }

  @GET @Path("/{id}/abi/aid") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Get ABI and Contract info",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, description = "intercept id"),
      new Parameter(name = "aid", in = ParameterIn.PATH, description = "ABI id")),    
    responses = Array(new ApiResponse(responseCode="200",description = "ABI info for intercept",content=Array(new Content(schema=new Schema(implementation = classOf[AbiContract])))))
  )
  def getAbiRoute(id:Interception.ID,aid: AbiStore.ID) = get {
    rejectEmptyResponse {
      onSuccess(getInterceptionAbi(id,aid)) { r =>
        metricGetCount.inc()
        complete(r)
      }      
    }
  }

  @GET @Path("/{id}/history") @Produces(Array(MediaType.TEXT_PLAIN))
  @Operation(tags = Array("intercept"),summary = "Download history as CSV file",
    parameters = Array(new Parameter(name = "id", in = ParameterIn.PATH, description = "intercept id")),
    responses = Array(new ApiResponse(responseCode="200",description = "History csv file",content=Array(new Content())))
  )
  def getHistoryRoute(id:String) = get {
    rejectEmptyResponse {
      onSuccess(getHistory(UUID(id))) { r =>
        //complete(StatusCodes.OK, List(`Content-Type`(`text/csv(UTF-8)`)), r)
        val entity = r.map(r => HttpEntity(ContentTypes.`text/csv(UTF-8)`, r))
        respondWithHeaders(RawHeader("Content-Disposition", s"attachment;filename=${id}.csv")) {
          complete(OK -> entity)
        }
      }
    }
  }

  @GET @Path("/search/{txt}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Search Interception by name",
    parameters = Array(new Parameter(name = "txt", in = ParameterIn.PATH, description = "search in name")),
    responses = Array(new ApiResponse(responseCode="200",description = "Found Interceptions",content=Array(new Content(schema=new Schema(implementation = classOf[Interceptions])))))
  )
  def getInterceptionSearch(txt: String) = get {
    rejectEmptyResponse {
      onSuccess(getInterceptionBySearch(txt)) { r =>
        encodeResponse { complete(r) }
      }
    }
  }

  @GET @Path("/") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"), summary = "Return all Interceptions",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of Interceptions",content = Array(new Content(schema = new Schema(implementation = classOf[Interceptions])))))
  )
  def getInterceptionsRoute() = get {
    parameters("history".as[Long].optional) { (history) => 
      metricGetCount.inc()
      encodeResponse { complete(getInterceptions(history)) }
    }
  }

  @DELETE @Path("/{id}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Delete Interception by id",
    parameters = Array(new Parameter(name = "id", in = ParameterIn.PATH, description = "Interception id (uuid)")),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Interception deleted",content = Array(new Content(schema = new Schema(implementation = classOf[Interception])))))
  )
  def deleteInterceptionRoute(id: String) = delete {
    onSuccess(deleteInterception(UUID(id))) { r =>
      metricDeleteCount.inc()
      complete((StatusCodes.OK, r))
    }
  }

  @POST @Path("/") @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Create Interception",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[InterceptionCreateReq])))),
    responses = Array(new ApiResponse(responseCode = "200", description = "Interception created",content = Array(new Content(schema = new Schema(implementation = classOf[InterceptionActionRes])))))
  )
  def createInterceptionRoute = post {
    entity(as[InterceptionCreateReq]) { interceptCreate =>
      onSuccess(createInterception(interceptCreate)) { r =>

        metricCreateCount.inc()
        complete((StatusCodes.Created, r))
      }
    }
  }

  @POST @Path("/{id}") @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Interception Command",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[InterceptionCommandReq])))),
    responses = Array(new ApiResponse(responseCode = "200", description = "Interception updated",content = Array(new Content(schema = new Schema(implementation = classOf[InterceptionActionRes])))))
  )
  def commandInterceptionRoute(id:Interception.ID) = post {
    entity(as[InterceptionCommandReq]) { interceptCommand =>
      onSuccess(commandInterception(interceptCommand.copy(id = Some(id)))) { r =>
        complete((StatusCodes.OK, r))
      }
    }
  }

// -------------------------------------------------------------------------------------------- Script --------------
  @GET @Path("/script/") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Return All Scripts",
    responses = Array(new ApiResponse(responseCode="200",description = "Scripts",content=Array(new Content(schema=new Schema(implementation = classOf[List[Script]])))))
  )
  def getScriptsRoute() = get {
    complete(getScripts())
  }

  @GET @Path("/script/{id}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("intercept"),summary = "Return Script by id",
    parameters = Array(new Parameter(name = "id", in = ParameterIn.PATH, description = "Script ID")),
    responses = Array(new ApiResponse(responseCode="200",description = "Script returned",content=Array(new Content(schema=new Schema(implementation = classOf[Script])))))
  )
  def getScriptRoute(id: String) = get {
    rejectEmptyResponse {
      onSuccess(getScript(id)) { r =>
        complete(r)
      }
    }
  }

  val corsAllow = CorsSettings(system.classicSystem)
    //.withAllowGenericHttpRequests(true)
    .withAllowCredentials(true)
    .withAllowedMethods(Seq(HttpMethods.OPTIONS,HttpMethods.GET,HttpMethods.POST,HttpMethods.PUT,HttpMethods.DELETE,HttpMethods.HEAD))

  override def routes: Route = cors(corsAllow) {
      concat(
        pathEndOrSingleSlash {
          concat(
            authenticate()(authn =>
              authorize(Permissions.isAdmin(authn)) {
                createInterceptionRoute  
              } ~
              getInterceptionsRoute()
            ),          
          )
        },
        pathPrefix("script") {
          authenticate()(authn => {
            pathPrefix(Segment) { id => 
              getScriptRoute(id)
            } ~ 
            pathEndOrSingleSlash {
              getScriptsRoute()
            }
          })
        },
        pathPrefix("search") {
          pathPrefix(Segment) { txt => 
            getInterceptionSearch(txt)
          }
        },
        pathPrefix("user") {
          authenticate()(authn => {
            pathPrefix(Segment) { uid => 
              authorize(Permissions.isUser(UUID(uid),authn)) {
                getInterceptionsFindByUserRoute(uid)
                
              }
            } ~ 
            pathEndOrSingleSlash {
              getScriptsRoute()
            }
          })
        },
        pathPrefix(Segment) { id => 
          authenticate()(authn =>
            pathPrefix("history") {
              getHistoryRoute(id)
            } ~
            pathPrefix("abi") { 
              pathPrefix(Segment) { aid => 
                getAbiRoute(UUID(id),aid)
              }
            }
          ) ~
          pathEndOrSingleSlash {
            authenticate()(authn =>
              getInterceptionRoute(id)
              ~ 
              deleteInterceptionRoute(id)
              ~
              commandInterceptionRoute(UUID(id))              
            )        
          }
        }
      )
  }
}
