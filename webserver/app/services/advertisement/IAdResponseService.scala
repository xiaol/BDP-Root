package services.advertisement

import java.io.{ BufferedWriter, OutputStreamWriter }
import java.net.{ InetSocketAddress, Socket, SocketAddress }
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.{ AdRequest, AdResponse, Creative }
import commons.models.news.NewsFeedResponse
import commons.utils.Sha1Utils
import dao.userprofiles.UserDeviceDAO
import io.netty.handler.codec.http.{ DefaultHttpHeaders, HttpHeaders }
import org.asynchttpclient.{ DefaultAsyncHttpClient, ListenableFuture, Response }
import org.springframework.util.Base64Utils
import play.api.libs.json.Json
import utils.AdConfig._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by zhangshl on 16/9/9.
 */
@ImplementedBy(classOf[AdResponseService])
trait IAdResponseService {
  def getAdResponse(body: String, remoteAddress: Option[String], uid: Long): Future[Seq[NewsFeedResponse]]
}

class AdResponseService @Inject() (val userDeviceDAO: UserDeviceDAO) extends IAdResponseService {

  def getAdResponse(body: String, remoteAddress: Option[String], uid: Long): Future[Seq[NewsFeedResponse]] = Future {
    //替换nginx传过来的真实ip
    val requestbody: String = remoteAddress match {
      case Some(ip) =>
        val request: AdRequest = Json.parse(body).as[AdRequest]
        val adRequest = request.copy(device = request.device.copy(ip = Some(ip)))
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
    val f: ListenableFuture[Response] = asyncHttpClient.preparePost(adurl).setBody(requestbody).setHeaders(headers).execute()
    val response: String = f.get().getResponseBody
    asyncHttpClient.close()
    val adResponse: AdResponse = Json.parse(response).as[AdResponse]
    if (adResponse.data.nonEmpty && adResponse.data.get.adspace.nonEmpty && adResponse.data.get.adspace.get.head.creative.nonEmpty) {
      val list: List[Creative] = adResponse.data.get.adspace.get.head.creative.get
      val seq: Seq[NewsFeedResponse] = list.map {
        case creative: Creative =>
          NewsFeedResponse.from(creative).copy(adresponse = Some(adResponse))
      }.toSeq
      seq
    } else {
      Seq[NewsFeedResponse]()
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
      //    val asyncHttpClient = new DefaultAsyncHttpClient()
      //    asyncHttpClient.prepareGet(deleteAdUrl_v + "?nid=" + nid).execute().get()
      //    asyncHttpClient.close()
    }
  }
}
