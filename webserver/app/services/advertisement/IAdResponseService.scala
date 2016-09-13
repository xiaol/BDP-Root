package services.advertisement

import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.{ Creative, AdResponse }
import commons.models.news.NewsFeedResponse
import commons.utils.Sha1Utils
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
  def getAdResponse(body: String): Future[Seq[NewsFeedResponse]]
}

class AdResponseService @Inject() () extends IAdResponseService {

  def getAdResponse(body: String): Future[Seq[NewsFeedResponse]] = {
    {
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
        val f: ListenableFuture[Response] = asyncHttpClient.preparePost(adurl).setBody(body).setHeaders(headers).execute()
        val r: String = f.get().getResponseBody
        r
      }

      response.map { response =>
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
}