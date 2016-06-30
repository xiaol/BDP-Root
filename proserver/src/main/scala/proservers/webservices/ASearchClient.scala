package proservers.webservices

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.stream.Materializer
import akka.stream.scaladsl.Flow

import scala.concurrent.{ ExecutionContext, Future }
import proservers.clients.HttpClient
import commons.models.community._
import play.api.libs.json._

import scala.util.Try

/**
 * Created by zhange on 2016-06-14.
 *
 */

case class GetASearchsRequest(key: String)

trait IASearchApi {
  def getASearchs(key: String): Future[Option[ASearchs]]

  def getASearchs[T](implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Flow[(GetASearchsRequest, T), (Option[ASearchs], T), NotUsed]
}

object ASearchClient {

  def apply()(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext, log: LoggingAdapter) = new ASearchApiImpl

  def encode(key: String): String = HttpClient.encode(key)

  def responseToString(resp: HttpResponse)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext, log: LoggingAdapter): Future[String] =
    HttpClient.responseToString(resp)

  def mapResponseToASearchResult(json: String)(implicit reader: Reads[ASearchTemp]): Option[ASearchs] =
    Json.parse(json).validate[ASearchTemps] match {
      case JsSuccess(ASearchTemps(items), _) if items.nonEmpty => Some(ASearchs(items.map(ASearch.from)))
      case _                                                   => None
    }

  def getWeatherRequestFlow[T]: Flow[(GetASearchsRequest, T), (HttpRequest, T), NotUsed] =
    Flow[(GetASearchsRequest, T)].map { case (request, id) => (HttpClient.mkGetRequest(s"/search?key=${encode(request.key)}"), id) }

  def mapResponseToASearchsResultFlow[T](implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext, eader: Reads[ASearchTemp]): Flow[(Try[HttpResponse], T), (Option[ASearchs], T), NotUsed] =
    HttpClient.responseToString[T].map { case (json, id) => (mapResponseToASearchResult(json), id) }
}

class ASearchApiImpl()(implicit val system: ActorSystem, val ec: ExecutionContext, val mat: Materializer, val log: LoggingAdapter) extends IASearchApi {
  import ASearchClient._

  private val client = HttpClient("asearch")

  override def getASearchs(key: String): Future[Option[ASearchs]] =
    client.get(s"/search?key=${encode(key)}").
      flatMap(responseToString)
      .map(mapResponseToASearchResult)

  override def getASearchs[T](implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Flow[(GetASearchsRequest, T), (Option[ASearchs], T), NotUsed] =
    getWeatherRequestFlow[T]
      .via(client.cachedHostConnectionFlow[T])
      .via(mapResponseToASearchsResultFlow[T])
}