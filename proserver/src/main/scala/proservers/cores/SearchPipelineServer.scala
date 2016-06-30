package proservers.cores

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings

import scala.util.{ Failure, Success }
import akka.util.Timeout

import scala.concurrent.duration._
import play.api.libs.json._
import java.net._

import HttpMethods._
import akka.stream.scaladsl.{ Flow, Sink, Source }
import headers._
import commons.models.community._
import commons.messages.pipeline.SearchPipelineTask
import proservers.utils.Config

import scala.concurrent.Future

/**
 * Created by zhange on 2016-05-19.
 *
 */

class SearchPipelineServer extends Actor with Config {
  import context.dispatcher
  val logger = Logging(context.system, this)
  implicit val timeout: Timeout = 20.seconds
  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  override def receive = {
    case SearchPipelineTask(task) =>
      val superior = sender; process(superior, task)
    case _ =>
  }

  private def process(superior: ActorRef, task: String) = {

    //val response = Http(context.system).singleRequest(HttpRequest(GET, asearchURI.replace("[KEY]", URLEncoder.encode(task, "UTF-8")))) //,headers=List(`User-Agent`(""))

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http(context.system).outgoingConnection("60.28.29.37", 8088)
    val response =
      Source.single(HttpRequest(uri = "/search?key=[KEY]".replace("[KEY]", URLEncoder.encode(task, "UTF-8"))))
        .via(connectionFlow)
        .runWith(Sink.head)

    response.onComplete {
      case Success(HttpResponse(StatusCodes.OK, headers, entity, _)) =>
        Unmarshal(entity).to[String].onComplete {
          case Success(ent) =>
            Json.parse(ent).validate[ASearchTemps] match {
              case JsSuccess(ASearchTemps(items), _) if items.nonEmpty =>
                //println(ASearchs(items.map(ASearch.from)))
                superior ! ASearchs(items.map(ASearch.from))
              case JsSuccess(_, _) => superior ! ASearchs(List[ASearch]())
              case JsError(err)    => superior ! ASearchs(List[ASearch]())
            }
          case Failure(err) => superior ! ASearchs(List[ASearch]()); logger.error(s"SearchPipelineServerErr.process: $task, Unmarshal: ${err.getMessage}")
        }
      case Success(HttpResponse(statusCode, _, _, _)) =>
        superior ! ASearchs(List[ASearch]()); logger.error(s"SearchPipelineServerErr.process: $task, Request: StatusCode is $statusCode")
      case Failure(err) => superior ! ASearchs(List[ASearch]()); logger.error(s"SearchPipelineServerErr.process: $task, Request: ${err.getMessage}")
    }
  }
}

object SearchPipelineServer {
  def props: Props = Props[SearchPipelineServer]
}
