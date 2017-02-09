package controllers

import javax.inject.Inject

import commons.models.advertisement.RequestParams
import commons.models.news._
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
import services.video.VideoService
import utils.Response._
import utils.ResponseRecommand.{ DataEmptyError => _, DataInvalidError => _, ServerSucced => _ }

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.Random
import commons.utils.Base64Utils.decodeBase64

/**
 * Created by zhangshl on 16/7/27.
 */
class NewsRecommendController @Inject() (val userService: UserService, val newsRecommendService: NewsRecommendService, val videoService: VideoService, val newsService: NewsService, val pvdetailService: PvdetailService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def loadFeedNew(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid, "NewsRecommendController.loadFeedNew", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    var newcount = count
    if (count > 14) {
      newcount = 14
    }
    uid match {
      case uid: Long if uid > 0 => cid match {
        case 1L => newsRecommendService.loadFeedByRecommendsNew(uid, page, newcount, tcursor, t).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
        case _ => newsService.loadFeedByChannel(uid: Long, cid, sechidOpt, page, newcount, tcursor, nid).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
      }
      case _ => Future.successful(DataEmptyError(s"$uid"))
    }

  }

  def refreshFeedNew(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid, "NewsRecommendController.refreshFeedNew", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    var newcount = count
    if (count > 14) {
      newcount = 14
    }
    uid match {
      case uid: Long if uid > 0 => cid match {
        case 1L => newsRecommendService.refreshFeedByRecommendsNew(uid, page, newcount, tcursor, t).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
        case _ => newsService.refreshFeedByChannel(uid: Long, cid, sechidOpt, page, newcount, tcursor, nid).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
      }
      case _ => Future.successful(DataEmptyError(s"$uid"))
    }

  }

  def loadFeedWithAd = Action.async(parse.json) { implicit request =>
    request.body.validate[RequestParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        pvdetailService.insert(PvDetail(requestParams.uid, "NewsRecommendController.loadFeedWithAd", LocalDateTime.now(), request.headers.get("X-Real-IP")))
        var newcount: Long = requestParams.c.getOrElse(14)
        if (newcount > 14) {
          newcount = 14
        }
        requestParams.uid match {
          case uid: Long if uid > 0 => requestParams.cid match {
            case 1L => newsRecommendService.loadFeedWithAd(requestParams.uid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), requestParams.t.getOrElse(0), request.headers.get("X-Real-IP"), requestParams.v).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news)) else removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news))
              case _                                            => DataEmptyError(s"$requestParams")
            }
            //视频
            case 44L => videoService.loadFeedWithAd(requestParams.uid, requestParams.cid, None, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), request.headers.get("X-Real-IP"), requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"$requestParams")
            }
            case _ => newsService.loadFeedByChannelWithAd(requestParams.uid, requestParams.cid, None, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), request.headers.get("X-Real-IP"), requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news)) else removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news))
              case _                                            => DataEmptyError(s"$requestParams")
            }
          }
          case _ => Future.successful(DataEmptyError(s"$requestParams"))
        }

    }
  }

  def refreshFeedWithAd = Action.async(parse.json) { request =>
    request.body.validate[RequestParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        pvdetailService.insert(PvDetail(requestParams.uid, "NewsRecommendController.refreshFeedWithAd", LocalDateTime.now(), request.headers.get("X-Real-IP")))
        var newcount: Long = requestParams.c.getOrElse(9)
        if (newcount > 9) {
          newcount = 9
        }
        requestParams.uid match {
          case uid: Long if uid > 0 => requestParams.cid match {
            case 1L => newsRecommendService.refreshFeedWithAd(requestParams.uid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), requestParams.t.getOrElse(0), request.headers.get("X-Real-IP"), requestParams.v).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news)) else removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news))
              case _                                            => DataEmptyError(s"$requestParams")
            }
            //视频
            case 44L => videoService.refreshFeedWithAd(requestParams.uid, requestParams.cid, None, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), request.headers.get("X-Real-IP"), requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"$requestParams")
            }
            case _ => newsService.refreshFeedByChannelWithAd(requestParams.uid, requestParams.cid, None, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), request.headers.get("X-Real-IP"), requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news)) else removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news))
              case _                                            => DataEmptyError(s"$requestParams")
            }
          }
          case _ => requestParams.cid match {
            case 1L => newsService.refreshFeedByRecommends(requestParams.p.getOrElse(1), newcount, requestParams.tcr, requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"${requestParams.cid}, ${requestParams.p}, ${requestParams.c}, $requestParams.tcr}")
            }
            case _ => newsService.refreshFeedByChannel(requestParams.uid, requestParams.cid, requestParams.scid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"${requestParams.cid}, ${requestParams.p}, ${requestParams.c}, $requestParams.tcr}")
            }
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

  //美女频道,图片需大于1
  final private def removeOnePicChin26(news: Seq[NewsFeedResponse]): Seq[NewsFeedResponse] = {
    news.filter { news =>
      if (news.channel == 26 && news.imgs.isEmpty) {
        false
      } else if (news.channel == 26 && news.imgs.nonEmpty && news.imgs.get.size <= 1) {
        false
      } else
        true
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
    imags.map { url => url.replace("http://pro-pic.deeporiginalx.com", "https://bdp-images.oss-cn-hangzhou.aliyuncs.com").replace("http://bdp-pic.deeporiginalx.com", "https://bdp-images.oss-cn-hangzhou.aliyuncs.com") }
  }

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

}