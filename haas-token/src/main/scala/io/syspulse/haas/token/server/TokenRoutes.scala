package io.syspulse.haas.token.server

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

import io.jvm.uuid._

import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import jakarta.ws.rs.{Consumes, POST, GET, DELETE, Path, Produces}
import jakarta.ws.rs.core.MediaType

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter

import io.syspulse.skel.service.Routeable
import io.syspulse.skel.service.CommonRoutes

import io.syspulse.skel.Command

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import io.syspulse.skel.auth.permissions.rbac.Permissions
import io.syspulse.skel.auth.RouteAuthorizers

import io.syspulse.haas.token.server.TokenProto
import io.syspulse.haas.token._
import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID
import io.syspulse.haas.core.serde.TokenJson

import io.syspulse.haas.token.store.TokenRegistry
import io.syspulse.haas.token.store.TokenRegistry._
import io.syspulse.haas.token.server._


@Path("/api/v1/token")
class TokenRoutes(registry: ActorRef[Command])(implicit context: ActorContext[_]) extends CommonRoutes with Routeable with RouteAuthorizers {
  //val log = Logger(s"${this}")
  implicit val system: ActorSystem[_] = context.system
  
  implicit val permissions = Permissions()

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import TokenJson._
  import TokenProto._
  
  // registry is needed because Unit-tests with multiple Routes in Suites will fail (Prometheus libary quirk)
  val cr = new CollectorRegistry(true);
  val metricGetCount: Counter = Counter.build().name("skel_token_get_total").help("Token gets").register(cr)
  val metricDeleteCount: Counter = Counter.build().name("skel_token_delete_total").help("Token deletes").register(cr)
  val metricCreateCount: Counter = Counter.build().name("skel_token_create_total").help("Token creates").register(cr)
  
  def getTokens(): Future[Tokens] = registry.ask(GetTokens)
  def getToken(id: Token.ID): Future[Option[Token]] = registry.ask(GetToken(id, _))
  def getTokenBySearch(txt: String): Future[Tokens] = registry.ask(SearchToken(txt, _))
  def getTokenByTyping(txt: String): Future[Tokens] = registry.ask(TypingToken(txt, _))

  def createToken(tokenCreate: TokenCreateReq): Future[Token] = registry.ask(CreateToken(tokenCreate, _))
  def deleteToken(id: Token.ID): Future[TokenActionRes] = registry.ask(DeleteToken(id, _))
  def randomToken(): Future[Token] = registry.ask(RandomToken(_))


  @GET @Path("/{id}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("token"),summary = "Return Token by id",
    parameters = Array(new Parameter(name = "id", in = ParameterIn.PATH, description = "Token id (gecko)")),
    responses = Array(new ApiResponse(responseCode="200",description = "Token returned",content=Array(new Content(schema=new Schema(implementation = classOf[Token])))))
  )
  def getTokenRoute(id: String) = get {
    rejectEmptyResponse {
      onSuccess(getToken(id)) { r =>
        metricGetCount.inc()
        complete(r)
      }
    }
  }

  @GET @Path("/search/{txt}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("token"),summary = "Search Token by term",
    parameters = Array(new Parameter(name = "txt", in = ParameterIn.PATH, description = "search term")),
    responses = Array(new ApiResponse(responseCode="200",description = "Found Tokens",content=Array(new Content(schema=new Schema(implementation = classOf[Tokens])))))
  )
  def getTokenSearch(txt: String) = get {
    rejectEmptyResponse {
      onSuccess(getTokenBySearch(txt)) { r =>
        complete(r)
      }
    }
  }

  @GET @Path("/typing/{txt}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("token"),summary = "Search Token by Type-Ahead (first letters)",
    parameters = Array(new Parameter(name = "txt", in = ParameterIn.PATH, description = "search letters")),
    responses = Array(new ApiResponse(responseCode="200",description = "Found Tokens",content=Array(new Content(schema=new Schema(implementation = classOf[Tokens])))))
  )
  def getTokenTyping(txt: String) = get {
    rejectEmptyResponse {
      onSuccess(getTokenByTyping(txt)) { r =>
        complete(r)
      }
    }
  }



  @GET @Path("/") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("token"), summary = "Return all Tokens",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of Tokens",content = Array(new Content(schema = new Schema(implementation = classOf[Tokens])))))
  )
  def getTokensRoute() = get {
    metricGetCount.inc()
    complete(getTokens())
  }

  @DELETE @Path("/{id}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("token"),summary = "Delete Token by id",
    parameters = Array(new Parameter(name = "id", in = ParameterIn.PATH, description = "Token id (uuid)")),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Token deleted",content = Array(new Content(schema = new Schema(implementation = classOf[Token])))))
  )
  def deleteTokenRoute(id: String) = delete {
    onSuccess(deleteToken(id)) { r =>
      metricDeleteCount.inc()
      complete((StatusCodes.OK, r))
    }
  }

  @POST @Path("/") @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("token"),summary = "Create Token",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[TokenCreateReq])))),
    responses = Array(new ApiResponse(responseCode = "200", description = "Token created",content = Array(new Content(schema = new Schema(implementation = classOf[TokenActionRes])))))
  )
  def createTokenRoute = post {
    entity(as[TokenCreateReq]) { tokenCreate =>
      onSuccess(createToken(tokenCreate)) { r =>
        metricCreateCount.inc()
        complete((StatusCodes.Created, r))
      }
    }
  }

  def createTokenRandomRoute() = post { 
    onSuccess(randomToken()) { r =>
      metricCreateCount.inc()
      complete((StatusCodes.Created, r))
    }
  }

  val corsAllow = CorsSettings(system.classicSystem).withAllowGenericHttpRequests(true)

  override def routes: Route = cors(corsAllow) {
      concat(
        pathEndOrSingleSlash {
          concat(
            authenticate()(authn =>
              authorize(Permissions.isAdmin(authn)) {              
                createTokenRoute  
              } ~
              getTokensRoute()
            ),          
          )
        },
        pathSuffix("random") {
          createTokenRandomRoute()
        },
        pathPrefix("search") {
          pathPrefix(Segment) { txt => 
            getTokenSearch(txt)
          }
        },
        pathPrefix("typing") {
          pathPrefix(Segment) { txt => 
            getTokenTyping(txt)
          }
        },
        pathPrefix(Segment) { id =>         
          pathEndOrSingleSlash {
            authenticate()(authn =>              
              getTokenRoute(id)
              ~ 
              authorize(Permissions.isAdmin(authn)) {
                deleteTokenRoute(id)
              }
            )            
          }
        }
      )
    }
    
}
