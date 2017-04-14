package services.advertisement

import java.io.{ BufferedWriter, OutputStreamWriter }
import java.net.{ InetSocketAddress, Socket, SocketAddress }
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.{ AdRequest, AdResponse, Creative }
import commons.models.news.NewsFeedResponse
import commons.utils.Sha1Utils
import dao.userprofiles.UserDeviceDAO
import io.netty.handler.codec.http.{ DefaultHttpHeaders, HttpHeaders }
import org.asynchttpclient.{ DefaultAsyncHttpClient, ListenableFuture, Response }
import org.springframework.util.Base64Utils
import play.api.Logger
import play.api.libs.json.Json
import utils.AdConfig._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

/**
 * Created by zhangshl on 16/9/9.
 */
@ImplementedBy(classOf[AdResponseService])
trait IAdResponseService {
  def getAdNewsFeedResponse(body: String, remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]]
  def getAdResponse(body: String, remoteAddress: Option[String]): Future[Option[AdResponse]]
}

class AdResponseService @Inject() (val userDeviceDAO: UserDeviceDAO) extends IAdResponseService {

  def getAdNewsFeedResponse(body: String, remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    try {
      val result: Future[Option[AdResponse]] = getAdResponse(body, remoteAddress)

      result.map { response =>
        response match {
          case Some(adResponse: AdResponse) =>
            if (adResponse.data.nonEmpty && adResponse.data.get.adspace.nonEmpty && adResponse.data.get.adspace.get.head.creative.nonEmpty) {
              val list: List[Creative] = adResponse.data.get.adspace.get.head.creative.get
              val seq: Seq[NewsFeedResponse] = list.map {
                case creative: Creative =>
                  NewsFeedResponse.from(creative).copy(adresponse = Some(adResponse))
              }
              seq
            } else {
              Seq[NewsFeedResponse]()
            }
          case _ => Seq[NewsFeedResponse]()
        }
      }
    } catch {
      case ex: Exception =>
        Logger.error(s"Within AdResponseService.getAdNewsFeedResponse(): ${ex.getMessage}")
        Future { Seq[NewsFeedResponse]() }
    }
  }

  def getAdResponse(body: String, remoteAddress: Option[String]): Future[Option[AdResponse]] = {
    try {
      //替换nginx传过来的真实ip
      val requestbody: String = remoteAddress match {
        case Some(ip) =>
          val request: AdRequest = Json.parse(body).as[AdRequest]
          val adRequest = request.copy(device = request.device.copy(ip = Some(ip)))
          Json.toJson(adRequest).toString()
        case _ => body
      }

      //组装TOKEN
      val nowtime: Long = new Date().getTime / 1000
      val sign: String = Sha1Utils.encodeSha1(adappkey + "|" + nowtime)
      val str = adappid + "|" + nowtime + "|" + sign
      val X_TOKEN: String = Base64Utils.encodeToString(str.getBytes)

      //组装header
      val asyncHttpClient = new DefaultAsyncHttpClient()
      val headers = new DefaultHttpHeaders()
      headers.add(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_JSON)
      headers.add("X-TOKEN", X_TOKEN)

      //获取广告
      val result: Future[String] = Future {
        val f: ListenableFuture[Response] = asyncHttpClient.preparePost(adurl).setBody(requestbody).setHeaders(headers).execute()
        val response: String = f.get().getResponseBody
        asyncHttpClient.close()
        response
      }

      //广告300毫秒超时
      try {
        Await.result(result, Duration(300, TimeUnit.MILLISECONDS))
        val adResponse = result.map { response =>
          val adResponse: AdResponse = Json.parse(response).as[AdResponse]
          if (adResponse.data.nonEmpty && adResponse.data.get.adspace.nonEmpty && adResponse.data.get.adspace.get.head.creative.nonEmpty) {
            Some(adResponse)
          } else {
            None
          }
        }
        adResponse
      } catch {
        case ex: Exception => Future { None }
      } finally {
        try {
          if (!asyncHttpClient.isClosed)
            asyncHttpClient.close()
        } catch { case ex: Exception => Logger.error(s"Within AdResponseService.getAdResponse(): ${ex.getMessage}") }
      }

    } catch {
      case ex: Exception =>
        Logger.error(s"Within AdResponseService.getAdResponse(): ${ex.getMessage}")
        Future { None }
    }
  }

  def deleteAd(nid: Long) {
    Future.successful {
      val path: String = deleteAdPath_v + "?nid=" + nid
      val dest: SocketAddress = new InetSocketAddress(deleteAdHost_v, Integer.parseInt(deleteAdPort_v))
      val socket: Socket = new Socket()
      socket.connect(dest)
      val streamWriter: OutputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
      val bufferedWriter: BufferedWriter = new BufferedWriter(streamWriter);
      bufferedWriter.write("GET " + path + " HTTP/1.1\r\n");
      bufferedWriter.write("Host: " + deleteAdHost_v + "\r\n");
      bufferedWriter.write("\r\n");

      bufferedWriter.flush()
      streamWriter.flush()
      socket.close()
    }
  }
}
