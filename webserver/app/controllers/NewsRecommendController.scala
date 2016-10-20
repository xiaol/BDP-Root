package controllers

import javax.inject.Inject

import commons.models.advertisement.RequestParams
import commons.models.news.NewsFeedResponse
import jp.t2v.lab.play2.auth.AuthElement
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.BodyParsers.parse
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.news._
import services.users.UserService
import utils.Response._
import utils.ResponseRecommand.{ DataEmptyError => _, DataInvalidError => _, ServerSucced => _ }

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.Random
import commons.utils.Base64Utils.decodeBase64

/**
 * Created by zhangshl on 16/7/27.
 */
class NewsRecommendController @Inject() (val userService: UserService, val newsRecommendService: NewsRecommendService, val newsService: NewsService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def loadFeedNew(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int) = Action.async { implicit request =>
    cid match {
      case 1L => newsRecommendService.loadFeedByRecommendsNew(uid, page, count, tcursor, t).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
      case _ => newsService.loadFeedByChannel(cid, sechidOpt, page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
    }
  }

  def refreshFeedNew(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int) = Action.async { implicit request =>
    cid match {
      case 1L => newsRecommendService.refreshFeedByRecommendsNew(uid, page, count, tcursor, t).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
      case _ => newsService.refreshFeedByChannel(cid, sechidOpt, page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
    }
  }

  //  def loadFeedWithAd(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, adbody: String) = Action.async { implicit request =>
  //    cid match {
  //      case 1L => newsRecommendService.loadFeedWithAd(uid, page, count, tcursor, adbody: String).map {
  //        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
  //        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
  //      }
  //      case _ => newsService.loadFeedByChannel(cid, sechidOpt, page, count, tcursor).map {
  //        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
  //        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
  //      }
  //    }
  //  }

  //  implicit val rds = (
  //    (__ \ 'cid).read[Long] and
  //    (__ \ 'tcr).read[Long] and
  //    (__ \ 'uid).read[Long] and
  //    (__ \ 'b).read[String]
  //  ) tupled
  //
  //  def refreshFeedWithAd = Action.async(parse.json) { request =>
  //    request.body.validate[(Long, Long, Long, String)] match {
  //      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
  //      case JsSuccess((cid, tcursor, uid, adbody), _) =>
  //        cid match {
  //          case 1L => newsRecommendService.refreshFeedWithAd(uid, 1, 20, tcursor, decodeBase64(adbody)).map {
  //            case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == 1) mockRealTime(news) else news)
  //            case _                                            => DataEmptyError(s"$cid,  $tcursor")
  //          }
  //          case _ => newsService.refreshFeedByChannel(cid, None, 1, 20, tcursor).map {
  //            case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == 1) mockRealTime(news) else news)
  //            case _                                            => DataEmptyError(s"$cid,  $tcursor")
  //          }
  //        }
  //    }
  //  }

  def loadFeedWithAd = Action.async(parse.json) { implicit request =>
    request.body.validate[RequestParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        requestParams.cid match {
          case 1L => newsRecommendService.loadFeedWithAd(requestParams.uid, requestParams.p.getOrElse(1), requestParams.c.getOrElse(20), requestParams.tcr, decodeBase64(requestParams.b), requestParams.t.getOrElse(0)).map {
            case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(news) else news)
            case _                                            => DataEmptyError(s"$requestParams")
          }
          case _ => newsService.loadFeedByChannelWithAd(requestParams.cid, None, requestParams.p.getOrElse(1), requestParams.c.getOrElse(20), requestParams.tcr, decodeBase64(requestParams.b)).map {
            case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(news) else news)
            case _                                            => DataEmptyError(s"$requestParams")
          }
        }
    }
  }

  def refreshFeedWithAd = Action.async(parse.json) { request =>
    request.body.validate[RequestParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        requestParams.cid match {
          case 1L => newsRecommendService.refreshFeedWithAd(requestParams.uid, requestParams.p.getOrElse(1), requestParams.c.getOrElse(20), requestParams.tcr, decodeBase64(requestParams.b), requestParams.t.getOrElse(0)).map {
            case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(news) else news)
            case _                                            => DataEmptyError(s"$requestParams")
          }
          case _ => newsService.refreshFeedByChannelWithAd(requestParams.cid, None, requestParams.p.getOrElse(1), requestParams.c.getOrElse(20), requestParams.tcr, decodeBase64(requestParams.b)).map {
            case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(news) else news)
            case _                                            => DataEmptyError(s"$requestParams")
          }
        }
    }
  }

  final private def mockRealTime(news: Seq[NewsFeedResponse]): Seq[NewsFeedResponse] = {
    val mockIndexs = Random.shuffle((1 to news.length - 2).toList).slice(0, news.length / 4)
    news.map {
      case n: NewsFeedResponse if mockIndexs.contains(news.indexOf(n)) => n.copy(ptime = LocalDateTime.now().plusMinutes(-Random.nextInt(5)))
      case n: NewsFeedResponse                                         => n
    }
  }

}