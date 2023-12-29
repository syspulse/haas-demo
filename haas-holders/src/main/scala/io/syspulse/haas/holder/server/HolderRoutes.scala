package io.syspulse.haas.holder.server

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
import jakarta.ws.rs.{Consumes, POST, PUT, GET, DELETE, Path, Produces}
import jakarta.ws.rs.core.MediaType

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter

import io.syspulse.skel.service.Routeable
import io.syspulse.skel.service.CommonRoutes

import io.syspulse.skel.util.TimeUtil
import io.syspulse.skel.Command

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import io.syspulse.skel.auth.permissions.Permissions
import io.syspulse.skel.auth.RouteAuthorizers

import io.syspulse.haas.holder.server.HolderProto
import io.syspulse.haas.holder._
import io.syspulse.haas.core.Holders
import io.syspulse.haas.core.Holders.ID
import io.syspulse.haas.serde.HoldersJson

import io.syspulse.haas.holder.store.HolderRegistry
import io.syspulse.haas.holder.store.HolderRegistry._
import io.syspulse.haas.holder.server._
import io.syspulse.haas.core.Defaults
import scala.util.Try


@Path("/")
class HolderRoutes(registry: ActorRef[Command])(implicit context: ActorContext[_]) extends CommonRoutes with Routeable with RouteAuthorizers {
  //val log = Logger(s"${this}")
  implicit val system: ActorSystem[_] = context.system
  
  implicit val permissions = Permissions()

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import HoldersJson._
  import HolderProto._
  
  // registry is needed because Unit-tests with multiple Routes in Suites will fail (Prometheus libary quirk)
  val cr = new CollectorRegistry(true);
  val metricGetCount: Counter = Counter.build().name("skel_holder_get_total").help("Holders gets").register(cr)
  val metricDeleteCount: Counter = Counter.build().name("skel_holder_delete_total").help("Holders deletes").register(cr)
  val metricCreateCount: Counter = Counter.build().name("skel_holder_create_total").help("Holders creates").register(cr)
  val metricUpdateCount: Counter = Counter.build().name("skel_holder_update_total").help("Holders updates").register(cr)
  
  def getHolderss(): Future[Holderss] = registry.ask(GetHolderss)
  def getHoldersPage(id:String,ts0:Option[Long],ts1:Option[Long],from:Option[Int],size:Option[Int],limit:Option[Int]): Future[Holderss] = registry.ask(GetHoldersPage(id,ts0,ts1,from,size,limit, _))
  def getHolders(ids: Seq[Holders.ID]): Future[Holderss] = registry.ask(GetHolders(ids, _))    

  @GET @Path("/") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("holder"),summary = "Return Holders for mulitple Tokens",
    responses = Array(new ApiResponse(responseCode="200",description = "Holders",content=Array(new Content(schema=new Schema(implementation = classOf[Holderss])))))
  )
  def getAllRoute() = get {
    rejectEmptyResponse {
      onSuccess(getHolderss()) { r =>
        metricGetCount.inc()
        encodeResponse(complete(r))
      }
    }
  }

  @GET @Path("/{addr,addr}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("holder"),summary = "Return Holders for mulitple Tokens",
    parameters = Array(new Parameter(name = "id", in = ParameterIn.PATH, description = "Token Address")),
    responses = Array(new ApiResponse(responseCode="200",description = "Holders",content=Array(new Content(schema=new Schema(implementation = classOf[Holderss])))))
  )
  def getHoldersRoute(ids: String) = get {
    rejectEmptyResponse {
      onSuccess(getHolders(ids.split(",").toSeq)) { r =>
        metricGetCount.inc()
        encodeResponse(complete(r))
      }
    }
  }

  @GET @Path("/{addr}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("holder"), summary = "Return Holders for Token",
    parameters = Array(
      new Parameter(name = "ts0", in = ParameterIn.PATH, description = "Start Timestamp (optional)"),
      new Parameter(name = "ts1", in = ParameterIn.PATH, description = "End Timestamp (optional)"),
      new Parameter(name = "from", in = ParameterIn.PATH, description = "Page from (inclusive)"),
      new Parameter(name = "size", in = ParameterIn.PATH, description = "Page size"),
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of Holders",content = Array(new Content(schema = new Schema(implementation = classOf[Holderss])))))
  )
  def getHolderssRoute(addr:String) = get {
    parameters("ts0".as[String].optional, "ts1".as[String].optional,"from".as[Int].optional,"size".as[Int].optional,"limit".as[Int].optional) { (ts0,ts1,from,size,limit) =>
      onSuccess(getHoldersPage(addr,
        ts0.map(t => TimeUtil.wordToTs(t,0L).get),
        ts1.map(t => TimeUtil.wordToTs(t,Long.MaxValue).get),
        from,size,limit)) { r =>

        metricGetCount.inc()
        encodeResponse(complete(r))
    }}
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
              getAllRoute()
            ),          
          )
        },
        pathPrefix(Segment) { addr =>
          pathEndOrSingleSlash {
            authenticate()(authn =>              
              getHolderssRoute(addr)              
            )            
          }
        }
      )
    }
    
}
