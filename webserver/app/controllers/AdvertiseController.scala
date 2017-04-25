package controllers

import javax.inject.Inject

import commons.models.advertisement.{ RequestAdSourceParams, RequestAdvertiseParams }
import commons.models.news._
import commons.utils.Base64Utils.decodeBase64
import jp.t2v.lab.play2.auth.AuthElement
import org.joda.time.LocalDateTime
import play.api.libs.json._
import play.api.mvc.Results.Ok
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.advertisement.AdResponseService
import services.news.PvdetailService
import services.users.UserService
import utils.AdConfig._
import utils.{ AdSourceResponse, Response }
import utils.Response.{ ServerSucced, _ }

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

/**
 * Created by zhangshl on 16/7/27.
 */
class AdvertiseController @Inject() (val userService: UserService, val adResponseService: AdResponseService, val pvdetailService: PvdetailService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  /**
   * 获取广告来源,根据uid来进行分流,猎鹰广告api:1 ,广点通sdk:2 ,亦复广告api:3
   */
  def getAdSource = Action.async(parse.json) { request =>
    request.body.validate[RequestAdSourceParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        val uid = requestParams.uid % 10
        uid match {
          case i if i < lieyingapiWeight                                => Future.successful(AdSourceResponse.ServerSucced(1, -1, -1))
          case i if i - lieyingapiWeight < gdtsdkWeight                 => Future.successful(AdSourceResponse.ServerSucced(1, feedAdPos, relatedAdPos))
          case i if i - lieyingapiWeight - gdtsdkWeight < yifuapiWeight => Future.successful(AdSourceResponse.ServerSucced(1, -1, -1))
        }
    }

  }

  def getAd = Action.async(parse.json) { request =>
    request.body.validate[RequestAdvertiseParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        pvdetailService.insert(PvDetail(requestParams.uid, "AdvertiseController.getAd", LocalDateTime.now(), request.headers.get("X-Real-IP")))
        adResponseService.getAdNewsFeedResponse(decodeBase64(requestParams.b), request.headers.get("X-Real-IP")).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (requestParams.s.getOrElse(0) == 1) https(news) else news)
          case _                                            => DataEmptyError(s"$requestParams")
        }
    }
  }

  //http改https
  final private def https(news: Seq[NewsFeedResponse]): Seq[NewsFeedResponse] = {
    news.map { news =>
      news.imgs match {
        case Some(imags: List[String]) => news.copy(imgs = Some(https(imags)))
        case None                      => news
      }
    }
  }

  final private def https(imags: List[String]): List[String] = {
    imags.map { url => url.replace("http:", "https:") }
  }

}