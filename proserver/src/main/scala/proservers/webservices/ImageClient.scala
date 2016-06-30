package proservers.webservices

import java.util.regex.Pattern

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.headers.Location
import akka.stream.Materializer
import proservers.clients.HttpClient

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import StatusCodes._
import commons.utils.UserAgentUtils

/**
 * Created by zhange on 2016-06-15.
 *
 */

trait IImageClient {
  def download(url: String, followRedirect: Boolean = false): Future[Option[Array[Byte]]]
}

object ImageClient {

  def apply()(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext, log: LoggingAdapter) = new ImageClientImpl

  final val TLS_PATTERN: String = "https://"
  final val HTTP_PATTERN: String = "http://"

  final val TLS_PORT: Int = 443
  final val HTTP_PORT: Int = 80

  def extractRequestConfig(url: String): (String, Int, Boolean, String) = url match {
    case src if src.startsWith(TLS_PATTERN) =>
      val meta = extractHostAndQuery(url.replace(TLS_PATTERN, ""))
      val (host: String, port: Int) = extractPossiblePort(meta._1, port = TLS_PORT)
      (host, port, true, meta._2)
    case src =>
      val meta = extractHostAndQuery(url.replace(HTTP_PATTERN, ""))
      val (host: String, port: Int) = extractPossiblePort(meta._1, port = HTTP_PORT)
      (host, port, false, meta._2)
  }

  def extractHostAndQuery(uri: String): (String, String) = {
    if (uri.indexOf('/') != -1) uri.splitAt(uri.indexOf('/'))
    else (uri, "/")
  }

  def extractPossiblePort(host: String, port: Int = 80): (String, Int) = {
    Try {
      if (host.indexOf(':') != -1) {
        val hostPair = host.split(':')
        (hostPair.head, hostPair.last.toInt)
      } else (host, port)
    }.toOption.getOrElse((host, port))
  }

  def encode(uri: String) = {
    var encodeUri = uri
    val matcher = Pattern.compile("([\\u4e00-\\u9fa5]+)").matcher(uri)
    while (matcher.find()) {
      val matchWord = matcher.group(0)
      encodeUri = encodeUri.replace(matchWord, HttpClient.encode(matchWord))
    }
    encodeUri
  }

  def responseToBytes(resp: HttpResponse)(implicit ex: ExecutionContext, mat: Materializer): Future[Option[Array[Byte]]] = Unmarshal(resp.entity).to[Array[Byte]].map {
    case bytes if bytes.nonEmpty => Some(bytes)
    case _                       => None
  }
}

class ImageClientImpl(implicit val system: ActorSystem, val ec: ExecutionContext, val mat: Materializer, val log: LoggingAdapter) extends IImageClient with UserAgentUtils {
  import ImageClient._

  def download(url: String, followRedirect: Boolean = false): Future[Option[Array[Byte]]] = {
    val (host: String, port: Int, tls: Boolean, query: String) = extractRequestConfig(encode(url))
    val client = HttpClient(host, port, tls)
    val header: Map[String, String] = Map("User-Agent" -> web)

    client.get(query, headersMap = header).flatMap {
      case response @ HttpResponse(StatusCodes.OK, headers, _, _) => responseToBytes(response)
      case HttpResponse(MovedPermanently | Found | SeeOther | UseProxy | TemporaryRedirect, headers, _, _) => followRedirect match {
        case true => headers.collectFirst { case Location(uri) => uri.toString }.map { url =>
          val (host: String, port: Int, tls: Boolean, query: String) = extractRequestConfig(encode(url))
          val client = HttpClient(host, port, tls)
          client.get(query).flatMap {
            case response @ HttpResponse(StatusCodes.OK, _, _, _) => responseToBytes(response)
            case HttpResponse(status, _, _, _) =>
              log.error(s"Download redirect url: $url, $status")
              Future.successful(None)
          }
        }.getOrElse(Future.successful(None))
        case false => Future.successful(None)
      }
      case HttpResponse(status, _, _, _) =>
        log.error(s"Download url: $url, $status")
        Future.successful(None)
    }
  }
}

