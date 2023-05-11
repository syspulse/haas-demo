package io.syspulse.haas.supply.server

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
import akka.http.scaladsl.coding.Coders

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

import io.syspulse.skel.util.TimeUtil

import io.syspulse.skel.service.Routeable
import io.syspulse.skel.service.CommonRoutes

import io.syspulse.skel.Command

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import io.syspulse.skel.auth.permissions.rbac.Permissions
import io.syspulse.skel.auth.RouteAuthorizers

import io.syspulse.haas.supply.Supply
import io.syspulse.haas.supply.SupplyJson

import io.syspulse.haas.supply.store.SupplyRegistry
import io.syspulse.haas.supply.store.SupplyRegistry._
import io.syspulse.haas.supply.server._
import io.syspulse.haas.core.Defaults

import io.syspulse.haas.supply.Config


@Path("/")
class SupplyRoutes(registry: ActorRef[Command],config:Config)(implicit context: ActorContext[_]) extends CommonRoutes with Routeable with RouteAuthorizers {
  //val log = Logger(s"${this}")
  implicit val system: ActorSystem[_] = context.system
  
  implicit val permissions = Permissions()

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import SupplyJson._
  import SupplyProto._
  
  // registry is needed because Unit-tests with multiple Routes in Suites will fail (Prometheus libary quirk)
  val cr = new CollectorRegistry(true);
  val metricGetCount: Counter = Counter.build().name("skel_supply_get_total").help("Supply gets").register(cr)
  
  def getSupplys(): Future[Supplys] = registry.ask(GetSupplys)
  def getSupply(id: Supply.ID,ts0:Long,ts1:Long): Future[Option[Supply]] = registry.ask(GetSupply(id,ts0,ts1, _))
  def getSupplyByToken(tid: String,ts0:Long,ts1:Long): Future[Option[Supply]] = registry.ask(GetSupplyByToken(tid,ts0,ts1, _))
  def getSupplyLast(tokens:Seq[String],from:Int,size:Int): Future[Supplys] = registry.ask(GetSupplyLast(tokens,from,size, _))
  
  @GET @Path("/{id}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("supply"),summary = "Return Supply by id (UUID) and time range",
    parameters = Array(
      new Parameter(name = "id", in = ParameterIn.PATH, description = "Supply id (UUID)"),
      new Parameter(name = "ts0", in = ParameterIn.PATH, description = "Start Timestamp (millisec) (optional)"),
      new Parameter(name = "ts1", in = ParameterIn.PATH, description = "End Timestamp (millisec) (optional)")
    ),
    responses = Array(new ApiResponse(responseCode="200",description = "Supply returned",content=Array(new Content(schema=new Schema(implementation = classOf[Supply])))))
  )
  def getSupplyRoute(id: String) = get {
    rejectEmptyResponse {
      parameters("ts0".as[String].optional, "ts1".as[String].optional) { (ts0, ts1) =>
        onSuccess(getSupply( Supply(id), 
          TimeUtil.wordToTs(ts0.getOrElse(""),0L).get, TimeUtil.wordToTs(ts1.getOrElse(""),Long.MaxValue).get)) { r =>
          
          metricGetCount.inc()
          
          config.httpZip match {
            case "gzip" => encodeResponseWith(Coders.Gzip) { complete(r) }
            case _ => encodeResponse { complete(r) }
          }
      }}
    }
  }

  @GET @Path("/token/{tid}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("supply"),summary = "Return Supply by Token (address or Id) and time range",
    parameters = Array(
      new Parameter(name = "tid", in = ParameterIn.PATH, description = "Token (address or id)"),
      new Parameter(name = "ts0", in = ParameterIn.PATH, description = "Start Timestamp (millisec) (optional)"),
      new Parameter(name = "ts1", in = ParameterIn.PATH, description = "End Timestamp (millisec) (optional)")
    ),
    responses = Array(new ApiResponse(responseCode="200",description = "Supply returned",content=Array(new Content(schema=new Schema(implementation = classOf[Supply])))))
  )
  def getSupplyByTokenRoute(tid: String) = get {
    rejectEmptyResponse {
      parameters("ts0".as[String].optional, "ts1".as[String].optional) { (ts0, ts1) =>
        onSuccess(getSupplyByToken( tid, 
          TimeUtil.wordToTs(ts0.getOrElse(""),0L).get, TimeUtil.wordToTs(ts1.getOrElse(""),Long.MaxValue).get)) { r =>
          
          metricGetCount.inc()
          
          config.httpZip match {
            case "gzip" => encodeResponseWith(Coders.Gzip) { complete(r) }
            case _ => encodeResponse { complete(r) }
          }
        }}
    }
  }

  @GET @Path("/last") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("supply"),summary = "Return Last Supplys for list of tokens",
    parameters = Array(      
      new Parameter(name = "tokens", in = ParameterIn.PATH, description = "Tokens set (UNI,RBN). Empty for default set"),
      new Parameter(name = "from", in = ParameterIn.PATH, description = "Page index"),
      new Parameter(name = "size", in = ParameterIn.PATH, description = "Page Size"),
    ),
    responses = Array(new ApiResponse(responseCode="200",description = "Supply last set",content=Array(new Content(schema=new Schema(implementation = classOf[Seq[Supply]])))))
  )
  def getSupplyLastRoute() = get {    
    parameters("tokens".as[String].optional,"from".as[Int].optional,"size".as[Int].optional) { (tokens,from,size) =>
      onSuccess(getSupplyLast(
          if(tokens.isDefined) tokens.get.split(",",-1).toIndexedSeq else config.tokensDefault,
          from.getOrElse(0),
          size.getOrElse(Defaults.TOKEN_SET.size),
        )) { r => config.httpZip match {
            case "gzip" => encodeResponseWith(Coders.Gzip) { complete(r) }
            case _ => encodeResponse { complete(r) }
          }
      }}    
  }


  @GET @Path("/") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("supply"), summary = "Return all Supplys (gzip support)",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of Supplys",content = Array(new Content(schema = new Schema(implementation = classOf[Supplys])))))
  )
  def getSupplysRoute() = get {
    metricGetCount.inc()
    onSuccess(getSupplys()) { r => 
      config.httpZip match {
          case "gzip" => encodeResponseWith(Coders.Gzip) { complete(r) }
          case _ => encodeResponse { complete(r) }
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
        authenticate()(authn => 
          concat(            
            getSupplysRoute()            
          )
        )
      },
      pathPrefix("last") {
        pathEndOrSingleSlash {
          authenticate()(authn => 
            getSupplyLastRoute()              
          )            
        }
      },
      pathPrefix("token") {
        pathPrefix(Segment) { tid => 
          authenticate()(authn => 
            getSupplyByTokenRoute(tid)
          )
        }
      },
      pathPrefix(Segment) { id =>         
        pathEndOrSingleSlash {
          authenticate()(authn => 
            getSupplyRoute(id)              
          )            
        }
      }
    )
  }
}
