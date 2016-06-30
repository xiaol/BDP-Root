package proservers.clients

import java.net.URLEncoder

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Sink, Source }
import com.typesafe.config.Config

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

/**
 * Created by zhange on 2016-06-14.
 *
 */

class HttpClient(val config: HttpClientConfig)(implicit val system: ActorSystem, val log: LoggingAdapter, val ec: ExecutionContext, val mat: Materializer) extends RequestBuilding {
  import HttpClient._

  def cachedHostConnectionFlow[T](implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Flow[(HttpRequest, T), (Try[HttpResponse], T), NotUsed] =
    cachedConnectionPipeline(config)

  def get(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty): Future[HttpResponse] =
    singleRequestPipeline(mkRequest(RequestBuilding.Get, url, body, queryParamsMap, headersMap), config)

  def post(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty): Future[HttpResponse] =
    singleRequestPipeline(mkRequest(RequestBuilding.Post, url, body, queryParamsMap, headersMap), config)

  def put(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty): Future[HttpResponse] =
    singleRequestPipeline(mkRequest(RequestBuilding.Put, url, body, queryParamsMap, headersMap), config)

  def patch(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty): Future[HttpResponse] =
    singleRequestPipeline(mkRequest(RequestBuilding.Patch, url, body, queryParamsMap, headersMap), config)

  def delete(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty): Future[HttpResponse] =
    singleRequestPipeline(mkRequest(RequestBuilding.Delete, url, body, queryParamsMap, headersMap), config)

  def options(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty): Future[HttpResponse] =
    singleRequestPipeline(mkRequest(RequestBuilding.Options, url, body, queryParamsMap, headersMap), config)

  def head(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty): Future[HttpResponse] =
    singleRequestPipeline(mkRequest(RequestBuilding.Head, url, body, queryParamsMap, headersMap), config)

}

object HttpClient {
  import scala.collection.JavaConversions._

  def apply(configName: String)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext, log: LoggingAdapter): HttpClient =
    new HttpClient(HttpClientConfig(system.settings.config.getConfig(s"webservices.$configName")))

  def apply(config: Config)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext, log: LoggingAdapter): HttpClient =
    new HttpClient(HttpClientConfig(config))

  def apply(host: String, port: Int, tls: Boolean)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext, log: LoggingAdapter): HttpClient =
    new HttpClient(HttpClientConfig(host, port, tls))

  def encode(value: String): String = URLEncoder.encode(value, "UTF-8")

  def responseToString(response: HttpResponse)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Future[String] = response.status match {
    case status => Unmarshal(response.entity).to[String]
  }

  def responseToString[T](implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Flow[(Try[HttpResponse], T), (String, T), NotUsed] =
    Flow[(Try[HttpResponse], T)].mapAsync(1) {
      case (Failure(t), e)    => Future.failed(t)
      case (Success(resp), e) => responseToString(resp).map(str => (str, e))
    }

  def responseToBytes(response: HttpResponse)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Future[Option[Array[Byte]]] = response.status match {
    case StatusCodes.OK => Unmarshal(response.entity).to[Array[Byte]].map(Some(_))
    case _              => Future.successful(None)
  }

  def responseToBytes[T](implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Flow[(Try[HttpResponse], T), (Option[Array[Byte]], T), NotUsed] =
    Flow[(Try[HttpResponse], T)].mapAsync(1) {
      case (Failure(t), e)    => Future.failed(t)
      case (Success(resp), e) => responseToBytes(resp).map(bytes => (bytes, e))
    }

  def queryString(queryParams: Map[String, String]): String =
    if (queryParams.nonEmpty)
      "?" + queryParams
        .filterNot {
          case (key, value) ⇒ key.length == 0
        }.mapValues(encode)
        .toList
        .map {
          case (key, value) ⇒ s"$key=$value"
        }.mkString("&")
    else ""

  def header(key: String, value: String): Option[HttpHeader] =
    HttpHeader.parse(key, value) match {
      case ParsingResult.Ok(header, errors) => Option(header)
      case _                                => None
    }

  def headers(headersMap: Map[String, String]): List[HttpHeader] =
    headersMap.flatMap {
      case (key, value) => header(key, value)
    }.toList

  def tlsConnection(host: String, port: Int)(implicit system: ActorSystem): Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnectionHttps(host, port)

  def httpConnection(host: String, port: Int)(implicit system: ActorSystem): Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnection(host, port)

  def connection(config: HttpClientConfig)(implicit system: ActorSystem): Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    if (config.tls) tlsConnection(config.host, config.port) else
      httpConnection(config.host, config.port)

  def singleRequestPipeline(request: HttpRequest, config: HttpClientConfig)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Future[HttpResponse] =
    Source.single(request).via(connection(config)).runWith(Sink.head)

  def cachedConnection[T](host: String, port: Int)(implicit system: ActorSystem, mat: Materializer): Flow[(HttpRequest, T), (Try[HttpResponse], T), Http.HostConnectionPool] =
    Http().cachedHostConnectionPool[T](host, port)

  def cachedTlsConnection[T](host: String, port: Int)(implicit system: ActorSystem, mat: Materializer): Flow[(HttpRequest, T), (Try[HttpResponse], T), Http.HostConnectionPool] =
    Http().cachedHostConnectionPoolHttps[T](host, port)

  def cachedConnection[T](config: HttpClientConfig)(implicit system: ActorSystem, mat: Materializer): Flow[(HttpRequest, T), (Try[HttpResponse], T), Http.HostConnectionPool] =
    if (config.tls) cachedTlsConnection(config.host, config.port) else
      cachedConnection(config.host, config.port)

  def cachedConnectionPipeline[T](config: HttpClientConfig)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Flow[(HttpRequest, T), (Try[HttpResponse], T), NotUsed] =
    Flow[(HttpRequest, T)].via(cachedConnection(config))

  def mkEntity(body: String): HttpEntity.Strict = HttpEntity(ContentTypes.`application/json`, body)

  def mkRequest(requestBuilder: RequestBuilding#RequestBuilder, url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty) =
    requestBuilder(url + queryString(queryParamsMap), mkEntity(body)).addHeaders(headers(headersMap))

  def mkGetRequest(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty) =
    mkRequest(RequestBuilding.Get, url, body, queryParamsMap, headersMap)

  def mkPostRequest(url: String, body: String = "", queryParamsMap: Map[String, String] = Map.empty, headersMap: Map[String, String] = Map.empty) =
    mkRequest(RequestBuilding.Post, url, body, queryParamsMap, headersMap)
}