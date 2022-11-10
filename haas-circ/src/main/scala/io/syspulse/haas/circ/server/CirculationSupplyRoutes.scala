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
import javax.ws.rs.{Consumes, POST, GET, DELETE, Path, Produces}
import javax.ws.rs.core.MediaType

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter

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


@Path("/api/v1/circ")
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
      parameters("ts0".as[Long].optional, "ts1".as[Long].optional) { (ts0, ts1) =>
        if(ts0.isDefined && ts1.isDefined) 
          onSuccess(getCirculationSupply(CirculationSupply(id),ts0.get,ts1.get)) { r =>
            metricGetCount.inc()
            complete(r)
          }
        else 
          onSuccess(getCirculationSupply(CirculationSupply(id),0L,Long.MaxValue)) { r =>
            metricGetCount.inc()
            complete(r)
          }
      }
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

  override def routes: Route =
    concat(
      pathEndOrSingleSlash {
        authenticate()(authn => 
          concat(            
            getCirculationSupplysRoute()            
          )
        )
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
