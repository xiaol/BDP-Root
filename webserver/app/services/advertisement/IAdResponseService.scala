package services.advertisement

import java.io.{ BufferedWriter, OutputStreamWriter }
import java.net.{ Socket, InetSocketAddress, SocketAddress }
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.{ Device, AdRequest, Creative, AdResponse }
import commons.models.news.NewsFeedResponse
import commons.models.userprofiles.UserDevice
import commons.utils.Sha1Utils
import dao.userprofiles.UserDeviceDAO
import io.netty.handler.codec.http.{ HttpHeaders, DefaultHttpHeaders }
import org.asynchttpclient.{ Response, ListenableFuture, DefaultAsyncHttpClient }
import org.springframework.util.Base64Utils
import play.api.Logger
import play.api.libs.json.Json
import utils.AdConfig._

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by zhangshl on 16/9/9.
 */
@ImplementedBy(classOf[AdResponseService])
trait IAdResponseService {
  def getAdResponse(body: String, remoteAddress: Option[String], uid: Long): Future[Seq[NewsFeedResponse]]
}

class AdResponseService @Inject() (val userDeviceDAO: UserDeviceDAO) extends IAdResponseService {

  def getAdResponse(body: String, remoteAddress: Option[String], uid: Long): Future[Seq[NewsFeedResponse]] = {
    {
      //替换nginx传过来的真实ip
      val requestbody: String = remoteAddress match {
        case Some(ip) =>
          val request: AdRequest = Json.parse(body).as[AdRequest]
          val adRequest = request.copy(device = request.device.copy(ip = Some(ip)))
          //          val device: Device = adRequest.device
          //          userDeviceDAO.findByuid(uid.toString).map {
          //            _ match {
          //              case None =>
          //                userDeviceDAO.insert(UserDevice.from(device, uid))
          //              case _ =>
          //            }
          //          }.recover {
          //            case _ => Logger.error(s"Within userDeviceDAO.insert(UserDevice.from(device, uid))")
          //          }

          Json.toJson(adRequest).toString()
        case _ => body
      }

      val nowtime: Long = new Date().getTime / 1000
      val sign: String = Sha1Utils.encodeSha1(adappkey + "|" + nowtime)
      val str = adappid + "|" + nowtime + "|" + sign
      val X_TOKEN: String = Base64Utils.encodeToString(str.getBytes)

      val asyncHttpClient = new DefaultAsyncHttpClient()
      val headers = new DefaultHttpHeaders()
      headers.add(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_JSON)
      headers.add("X-TOKEN", X_TOKEN)
      //.executeRequest()可以设置超时时间
      val response: Future[String] = Future.successful {
        val f: ListenableFuture[Response] = asyncHttpClient.preparePost(adurl).setBody(requestbody).setHeaders(headers).execute()
        val r: String = f.get().getResponseBody
        asyncHttpClient.close()
        r
      }

      response.map { response =>
        if (!asyncHttpClient.isClosed) {
          asyncHttpClient.close()
        }
        val adResponse: AdResponse = Json.parse(response).as[AdResponse]
        if (adResponse.data.nonEmpty && adResponse.data.get.adspace.nonEmpty && adResponse.data.get.adspace.get.head.creative.nonEmpty) {
          val list: List[Creative] = adResponse.data.get.adspace.get.head.creative.get
          val seq: Seq[NewsFeedResponse] = list.map {
            case creative: Creative =>
              NewsFeedResponse.from(creative)
          }.toSeq
          seq
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.getAdResponse($body): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }

    //      org.apache.http.impl.nio.client.HttpAsyncClients

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
      //    val asyncHttpClient = new DefaultAsyncHttpClient()
      //    asyncHttpClient.prepareGet(deleteAdUrl_v + "?nid=" + nid).execute().get()
      //    asyncHttpClient.close()
    }
  }
}
