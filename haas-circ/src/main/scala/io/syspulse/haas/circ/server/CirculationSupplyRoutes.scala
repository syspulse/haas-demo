package io.syspulse.haas.circ.server

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

import io.syspulse.skel.cli.CliUtil

import io.syspulse.skel.service.Routeable
import io.syspulse.skel.service.CommonRoutes

import io.syspulse.skel.Command

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import io.syspulse.skel.auth.permissions.rbac.Permissions
import io.syspulse.skel.auth.RouteAuthorizers

import io.syspulse.haas.circ.CirculationSupply
import io.syspulse.haas.circ.Circulation
import io.syspulse.haas.circ.serde.CirculationSupplyJson

import io.syspulse.haas.circ.store.CirculationSupplyRegistry
import io.syspulse.haas.circ.store.CirculationSupplyRegistry._
import io.syspulse.haas.circ.server._


@Path("/")
class CirculationSupplyRoutes(registry: ActorRef[Command])(implicit context: ActorContext[_]) extends CommonRoutes with Routeable with RouteAuthorizers {
  //val log = Logger(s"${this}")
  implicit val system: ActorSystem[_] = context.system
  
  implicit val permissions = Permissions()

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import CirculationSupplyJson._
  import CirculationSupplyProto._
  
  // registry is needed because Unit-tests with multiple Routes in Suites will fail (Prometheus libary quirk)
  val cr = new CollectorRegistry(true);
  val metricGetCount: Counter = Counter.build().name("skel_circ_get_total").help("CirculationSupply gets").register(cr)
  
  def getCirculationSupplys(): Future[CirculationSupplys] = registry.ask(GetCirculationSupplys)
  def getCirculationSupply(id: CirculationSupply.ID,ts0:Long,ts1:Long): Future[Option[CirculationSupply]] = registry.ask(GetCirculationSupply(id,ts0,ts1, _))
  def getCirculationSupplyByToken(tid: String,ts0:Long,ts1:Long): Future[Option[CirculationSupply]] = registry.ask(GetCirculationSupplyByToken(tid,ts0,ts1, _))  
  
  @GET @Path("/{id}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("circ"),summary = "Return CirculationSupply by id (UUID) and time range",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, description = "CirculationSupply id (UUID)"),
      new Parameter(name = "ts0", in = ParameterIn.PATH, description = "Start Timestamp (millisec) (optional)"),
      new Parameter(name = "ts1", in = ParameterIn.PATH, description = "End Timestamp (millisec) (optional)")
    ),
    responses = Array(new ApiResponse(responseCode="200",description = "CirculationSupply returned",content=Array(new Content(schema=new Schema(implementation = classOf[CirculationSupply])))))
  )
  def getCirculationSupplyRoute(id: String) = get {
    rejectEmptyResponse {
      parameters("ts0".as[String].optional, "ts1".as[String].optional) { (ts0, ts1) =>
        onSuccess(getCirculationSupply( CirculationSupply(id), 
            CliUtil.wordToTs(ts0.getOrElse(""),0L).get, CliUtil.wordToTs(ts1.getOrElse(""),Long.MaxValue).get)) { r =>
            
            metricGetCount.inc()
            complete(r)
        }}
    }
  }

  @GET @Path("/token/{tid}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("circ"),summary = "Return CirculationSupply by Token (address or Id) and time range",
    parameters = Array(
      new Parameter(name = "tid", in = ParameterIn.PATH, description = "Token (address or id)"),
      new Parameter(name = "ts0", in = ParameterIn.PATH, description = "Start Timestamp (millisec) (optional)"),
      new Parameter(name = "ts1", in = ParameterIn.PATH, description = "End Timestamp (millisec) (optional)")
    ),
    responses = Array(new ApiResponse(responseCode="200",description = "CirculationSupply returned",content=Array(new Content(schema=new Schema(implementation = classOf[CirculationSupply])))))
  )
  def getCirculationSupplyByTokenRoute(tid: String) = get {
    rejectEmptyResponse {
      parameters("ts0".as[String].optional, "ts1".as[String].optional) { (ts0, ts1) =>
        onSuccess(getCirculationSupplyByToken( tid, 
            CliUtil.wordToTs(ts0.getOrElse(""),0L).get, CliUtil.wordToTs(ts1.getOrElse(""),Long.MaxValue).get)) { r =>
            
            metricGetCount.inc()
            complete(r)
        }}
    }
  }

  @GET @Path("/") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("circ"), summary = "Return all CirculationSupplys",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of CirculationSupplys",content = Array(new Content(schema = new Schema(implementation = classOf[CirculationSupplys])))))
  )
  def getCirculationSupplysRoute() = get {
    metricGetCount.inc()
    complete(getCirculationSupplys())
  }

  val corsAllow = CorsSettings(system.classicSystem).withAllowGenericHttpRequests(true)

  override def routes: Route = cors(corsAllow) {
    concat(
      pathEndOrSingleSlash {
        authenticate()(authn => 
          concat(            
            getCirculationSupplysRoute()            
          )
        )
      },
      pathPrefix("token") {
        pathPrefix(Segment) { tid => 
          authenticate()(authn => 
            getCirculationSupplyByTokenRoute(tid)
          )
        }
      },
      pathPrefix(Segment) { id =>         
        pathEndOrSingleSlash {
          authenticate()(authn => 
            getCirculationSupplyRoute(id)              
          )            
        }
      }
    )
  }
}
