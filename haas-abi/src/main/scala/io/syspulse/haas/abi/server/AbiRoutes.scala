package io.syspulse.haas.abi.server

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

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.parameters.RequestBody
// import javax.ws.rs.{Consumes, POST, GET, DELETE, Path, Produces}
// import javax.ws.rs.core.MediaType
import jakarta.ws.rs.{Consumes, POST, GET, DELETE, Path, Produces}
import jakarta.ws.rs.core.MediaType

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter

import io.syspulse.skel.service.Routeable
import io.syspulse.skel.service.CommonRoutes

import io.syspulse.skel.Command

import scala.util.Try
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import io.syspulse.skel.auth.permissions.Permissions
import io.syspulse.skel.auth.RouteAuthorizers

import io.syspulse.haas.abi._
import io.syspulse.haas.abi.store.AbiRegistry
import io.syspulse.haas.abi.store.AbiRegistry._
import io.syspulse.haas.abi.server._


@Path("/api/v1/abi")
class AbiRoutes(registry: ActorRef[Command])(implicit context: ActorContext[_]) extends CommonRoutes with Routeable with RouteAuthorizers {
  //val log = Logger(s"${this}")
  implicit val system: ActorSystem[_] = context.system
  
  implicit val permissions = Permissions()

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import AbiJson._
  import AbiProto._
  
  // registry is needed because Unit-tests with multiple Routes in Suites will fail (Prometheus libary quirk)
  val cr = new CollectorRegistry(true);
  val metricGetCount: Counter = Counter.build().name("skel_abi_get_total").help("Abi gets").register(cr)
  val metricDeleteCount: Counter = Counter.build().name("skel_abi_delete_total").help("Abi deletes").register(cr)
  val metricCreateCount: Counter = Counter.build().name("skel_abi_create_total").help("Abi creates").register(cr)
  
  def getAbis(entity:String,from:Option[Int],size:Option[Int]): Future[Abis] = registry.ask(GetAbis(entity,from,size, _))
  def getAbi(entity:String,id:String): Future[Try[Abi]] = registry.ask(GetAbi(entity,id, _))
  def getSearchAbi(entity:String,txt: String,from:Option[Int],size:Option[Int]): Future[Abis] = registry.ask(GetSearchAbi(entity,txt,from,size, _))
  def getTypingAbi(txt: String,from:Option[Int],size:Option[Int]): Future[Abis] = registry.ask(GetTypingAbi(txt,from,size, _))
  
  @GET @Path("/") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("abi"), summary = "Return all Objects",parameters = Array(
      new Parameter(name = "entity", in = ParameterIn.PATH, description = "Entity ('contract','event','function')"),
      new Parameter(name = "from", in = ParameterIn.PATH, description = "Page from (inclusive)"),
      new Parameter(name = "size", in = ParameterIn.PATH, description = "Page Size"),
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "List of Objects",content = Array(new Content(schema = new Schema(implementation = classOf[Abis])))))
  )
  def getAbisRoute() = get { 
    parameters("entity".as[String].optional,"from".as[Int].optional,"size".as[Int].optional) { (entity,from,size) =>
      onSuccess(getAbis(entity.getOrElse(""),from,size)) { r => 
        metricGetCount.inc()
        complete(r)
      }    
    }
  }


  @GET @Path("/{abi}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("abi"),summary = "Get abi by id",
    parameters = Array(
      new Parameter(name = "abi", in = ParameterIn.PATH, description = "Abi"),
      new Parameter(name = "entity", in = ParameterIn.PATH, description = "Entity ('contract','event','function')")
    ),
    responses = Array(new ApiResponse(responseCode="200",description = "Object found",content=Array(new Content(schema=new Schema(implementation = classOf[Abi])))))
  )
  def getAbiRoute(id: String) = get {  
    parameters("entity".as[String].optional) { (entity) =>
      rejectEmptyResponse {
        onSuccess(getAbi(entity.getOrElse("contract"),id)) { r =>
          metricGetCount.inc()
          complete(r)
        }
      }
    }
  }

  @GET @Path("/search/{txt}") @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(tags = Array("abis"),summary = "Search Objects by abis", parameters = Array(
      new Parameter(name = "entity", in = ParameterIn.PATH, description = "Entity ('','contract','event','function')"),
      new Parameter(name = "txt", in = ParameterIn.PATH, description = "Text to search (prefix only)"),
      new Parameter(name = "from", in = ParameterIn.PATH, description = "Page from (inclusive)"),
      new Parameter(name = "size", in = ParameterIn.PATH, description = "Page Size"),
    ),
    responses = Array(new ApiResponse(responseCode="200",description = "Abis found",content=Array(new Content(schema=new Schema(implementation = classOf[Abis])))))
  )
  def getAbiSearchRoute(txt: String) = get { 
    parameters("entity".as[String].optional,"from".as[Int].optional,"size".as[Int].optional) { (entity,from,size) =>
      rejectEmptyResponse {
        onSuccess(getSearchAbi(entity.getOrElse(""),txt,from,size)) { r =>
          metricGetCount.inc()
          complete(r)
        }
      }
    }
  }

  // @GET @Path("/typing/{txt}") @Produces(Array(MediaType.APPLICATION_JSON))
  // @Operation(tags = Array("abis"),summary = "Type-ahead search", parameters = Array(
  //     new Parameter(name = "txt", in = ParameterIn.PATH, description = "Prefix text match"),
  //     new Parameter(name = "from", in = ParameterIn.PATH, description = "Page from (inclusive)"),
  //     new Parameter(name = "size", in = ParameterIn.PATH, description = "Page Size"),
  //   ),
  //   responses = Array(new ApiResponse(responseCode="200",description = "Abis found",content=Array(new Content(schema=new Schema(implementation = classOf[Abis])))))
  // )
  // def getAbiTypingRoute(txt: String) = get { 
  //   parameters("from".as[Int].optional,"size".as[Int].optional) { (from,size) =>
  //     rejectEmptyResponse {
  //       onSuccess(getTypingAbi(txt,from,size)) { r =>
  //         metricGetCount.inc()
  //         complete(r)
  //       }
  //     }
  //   }
  // }

  val corsAllow = CorsSettings(system.classicSystem)
    //.withAllowGenericHttpRequests(true)
    .withAllowCredentials(true)
    .withAllowedMethods(Seq(HttpMethods.OPTIONS,HttpMethods.GET,HttpMethods.POST,HttpMethods.PUT,HttpMethods.DELETE,HttpMethods.HEAD))

  override def routes: Route = cors(corsAllow) {
    authenticate()(authn =>
      concat(
        pathEndOrSingleSlash {
          concat(
            getAbisRoute()            
          )
        },
        pathPrefix("search") {
          pathPrefix(Segment) { abis =>
            pathEndOrSingleSlash {
              getAbiSearchRoute(abis)            
            }
          }
        },
        // pathPrefix("typing") {
        //   pathPrefix(Segment) { txt =>
        //     pathEndOrSingleSlash {
        //       getAbiTypingRoute(txt)            
        //     }
        //   }
        // },
        pathPrefix(Segment) { abis =>
          pathEndOrSingleSlash {
            getAbiRoute(abis)            
          }
        }
      )
    )
  }
    
}
