package controllers

import javax.inject.Inject

import com.typesafe.config.Config
import commons.models.advertisement._
import commons.models.news._
import commons.utils.Base64Utils.decodeBase64
import jp.t2v.lab.play2.auth.AuthElement
import org.joda.time.LocalDateTime
import play.api.libs.json._
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.advertisement.AdResponseService
import services.news.PvdetailService
import services.users.UserService
import utils.AdConfig._
import utils.AdSourceResponse
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
        val channel = requestParams.ctype
        //1：奇点资讯， 2：黄历天气，3：纹字锁频，4：猎鹰浏览器，5：白牌 7:白牌应用汇
        val platform = requestParams.ptype //1.ios 2 android
        platform match {
          case 1 => channel match {
            case 1 =>
              obtainAdSource(adQDZX_I, platform, uid)
            case 2 =>
              obtainAdSource(adHLTQ_I, platform, uid)
            case 4 =>
              obtainAdSource(adLYLLQ_I, platform, uid)
            case _ => Future.successful(ParamsInvalidError(channel.toString))

          }

          case 2 => channel match {
            case 1 =>
              obtainAdSource(adQDZX_A, platform, uid)
            case 3 =>
              obtainAdSource(adWZSP_A, platform, uid)
            case 4 =>
              obtainAdSource(adLYLLQ_A, platform, uid)
            case 5 =>
              obtainAdSource(adBPYZ_A, platform, uid)
            case 7 =>
              obtainAdSource(adBPYYH_A, platform, uid)
            case _ => Future.successful(ParamsInvalidError(channel.toString))
          }
          case _ => Future.successful(ParamsInvalidError(platform.toString))
        }
    }

  }

  def obtainAdSource(config: Config, pType: Int, uid: Long): Future[Result] = {
    val adWeightType = if (pType == 1) adIosWeight else adAndroidWeight
    val adDisplayPosType = if (pType == 1) adIosPos else adAndroidPos
    val ad_weight = if (config.getConfig("weight").isEmpty) adWeightType else config.getConfig("weight")
    val ad_pos = if (config.getConfig("displayPosition").isEmpty) adDisplayPosType else config.getConfig("displayPosition")
    val lieyingapi = ad_weight.getInt("lieyingapi")
    val gdtsdk = ad_weight.getInt("gdtsdk")
    val yifuapi = ad_weight.getInt("yifuapi")
    val feedAdPos = ad_pos.getInt("feedAdPos")
    val relatedAdPos = ad_pos.getInt("relatedAdPos")
    val feedVideoAdPos = ad_pos.getInt("feedVideoAdPos")
    val relatedVideoAdPos = ad_pos.getInt("relatedVideoAdPos")
    uid match {
      case i if i < lieyingapi                    => Future.successful(AdSourceResponse.ServerSucced(1, -1, -1, -1, -1))
      case i if i - lieyingapi < gdtsdk           => Future.successful(AdSourceResponse.ServerSucced(2, feedAdPos, relatedAdPos, feedVideoAdPos, relatedVideoAdPos))
      case i if i - lieyingapi - gdtsdk < yifuapi => Future.successful(AdSourceResponse.ServerSucced(3, -1, -1, -1, -1))
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

  def getOriginalAd = Action.async(parse.json) { request =>
    request.body.validate[RequestAdvertiseParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        pvdetailService.insert(PvDetail(requestParams.uid, "AdvertiseController.getOriginalAd", LocalDateTime.now(), request.headers.get("X-Real-IP")))
        adResponseService.getAdResponse(decodeBase64(requestParams.b), request.headers.get("X-Real-IP")).map {
          case Some(news: AdResponse) => ServerSucced(news)
          case _                      => DataEmptyError(s"$requestParams")
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