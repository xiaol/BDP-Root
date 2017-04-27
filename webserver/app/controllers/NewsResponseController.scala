package controllers

import javax.inject.Inject

import commons.models.advertisement.RequestParams
import commons.models.news._
import commons.utils.Base64Utils._
import jp.t2v.lab.play2.auth.AuthElement
import org.joda.time.LocalDateTime
import play.api.libs.json.{ JsSuccess, JsError }
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.news._
import services.newsfeed._
import services.users.UserService
import services.video.VideoService
import utils.Response._
import utils.ResponseRecommand.{ DataEmptyError => _, DataInvalidError => _, ServerSucced => _ }

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.Random

/**
 * Created by zhange on 2016-05-16.
 *
 */
class NewsResponseController @Inject() (val qidianService: QidianWithCacheService, val feedChannelService: FeedChannelService, val newsNoUidService: NewsNoUidService,
                                        val userService: UserService, val videoService: VideoService, val pvdetailService: PvdetailService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def refreshFeedWithAd = Action.async(parse.json) { request =>
    request.body.validate[RequestParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        pvdetailService.insert(PvDetail(requestParams.uid, "NewsResponseController.refreshFeedWithAd", LocalDateTime.now(), request.headers.get("X-Real-IP")))
        var newcount: Long = requestParams.c.getOrElse(11)
        if (newcount > 11) {
          newcount = 11
        }
        requestParams.uid match {
          case uid: Long if uid > 0 => requestParams.cid match {
            case 1L => qidianService.refreshQidian(requestParams.uid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, requestParams.t.getOrElse(0), requestParams.v, Some(decodeBase64(requestParams.b)), request.headers.get("X-Real-IP"), requestParams.ads.getOrElse(1)).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news)) else removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news))
              case _                                            => DataEmptyError(s"$requestParams")
            }
            //视频
            case 44L => videoService.refreshFeedWithAd(requestParams.uid, requestParams.cid, requestParams.scid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), request.headers.get("X-Real-IP"), requestParams.nid, requestParams.ads.getOrElse(1)).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"$requestParams")
            }
            case _ => feedChannelService.refreshFeedByChannelWithAd(requestParams.uid, requestParams.cid, requestParams.scid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), request.headers.get("X-Real-IP"), requestParams.nid, requestParams.ads.getOrElse(1)).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news)) else removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news))
              case _                                            => DataEmptyError(s"$requestParams")
            }
          }
          //没有用户id, 走时间流刷新闻
          case _ => requestParams.cid match {
            case 1L => newsNoUidService.refreshQidian(requestParams.p.getOrElse(1), newcount, requestParams.tcr, requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"${requestParams.cid}, ${requestParams.p}, ${requestParams.c}, $requestParams.tcr}")
            }
            case _ => newsNoUidService.refreshFeedByChannel(requestParams.cid, requestParams.scid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"${requestParams.cid}, ${requestParams.p}, ${requestParams.c}, $requestParams.tcr}")
            }
          }
        }
    }
  }

  def loadFeedWithAd = Action.async(parse.json) { implicit request =>
    request.body.validate[RequestParams] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(requestParams, _) =>
        pvdetailService.insert(PvDetail(requestParams.uid, "NewsResponseController.loadFeedWithAd", LocalDateTime.now(), request.headers.get("X-Real-IP")))
        var newcount: Long = requestParams.c.getOrElse(14)
        if (newcount > 14) {
          newcount = 14
        }
        requestParams.uid match {
          case uid: Long if uid > 0 => requestParams.cid match {
            case 1L => qidianService.loadQidian(requestParams.uid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, requestParams.t.getOrElse(0), requestParams.v, Some(decodeBase64(requestParams.b)), request.headers.get("X-Real-IP"), requestParams.ads.getOrElse(1)).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news)) else removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news))
              case _                                            => DataEmptyError(s"$requestParams")
            }
            //视频
            case 44L => videoService.loadFeedWithAd(requestParams.uid, requestParams.cid, requestParams.scid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), request.headers.get("X-Real-IP"), requestParams.nid, requestParams.ads.getOrElse(1)).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"$requestParams")
            }
            case _ => feedChannelService.loadFeedByChannelWithAd(requestParams.uid, requestParams.cid, requestParams.scid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, decodeBase64(requestParams.b), request.headers.get("X-Real-IP"), requestParams.nid, requestParams.ads.getOrElse(1)).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk.getOrElse(1)) mockRealTime(removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news)) else removeOnePicChin26(if (requestParams.s.getOrElse(0) == 1) https(news) else news))
              case _                                            => DataEmptyError(s"$requestParams")
            }
          }
          //没有用户id, 走时间流刷新闻
          case _ => requestParams.cid match {
            case 1L => newsNoUidService.loadQidian(requestParams.p.getOrElse(1), newcount, requestParams.tcr, requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"${requestParams.cid}, ${requestParams.p}, ${requestParams.c}, $requestParams.tcr}")
            }
            case _ => newsNoUidService.loadFeedByChannel(requestParams.cid, requestParams.scid, requestParams.p.getOrElse(1), newcount, requestParams.tcr, requestParams.nid).map {
              case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == requestParams.tmk) mockRealTime(news) else news)
              case _                                            => DataEmptyError(s"${requestParams.cid}, ${requestParams.p}, ${requestParams.c}, $requestParams.tcr}")
            }
          }
        }
    }
  }

  def refreshFeedNew(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid, "NewsResponseController.refreshFeedNew", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    var newcount = count
    if (newcount > 11) {
      newcount = 11
    }
    uid match {
      case uid: Long if uid > 0 => cid match {
        case 1L => qidianService.refreshQidian(uid, page, newcount, tcursor, t, None, None, request.headers.get("X-Real-IP"), -1).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
        case _ => feedChannelService.refreshFeedByChannel(uid: Long, cid, sechidOpt, page, newcount, tcursor, nid).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
      }
      //没有用户id,走时间流刷新闻
      case _ => cid match {
        case 1L => newsNoUidService.refreshQidian(page, newcount, tcursor, nid).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
        case _ => newsNoUidService.refreshFeedByChannel(cid, sechidOpt, page, newcount, tcursor, nid).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
      }
    }
  }

  def loadFeedNew(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid, "NewsResponseController.loadFeedNew", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    var newcount = count
    if (count > 14) {
      newcount = 14
    }
    uid match {
      case uid: Long if uid > 0 => cid match {
        case 1L => qidianService.loadQidian(uid, page, newcount, tcursor, t, None, None, request.headers.get("X-Real-IP"), -1).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
        case _ => feedChannelService.loadFeedByChannel(uid: Long, cid, sechidOpt, page, newcount, tcursor, nid).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
      }
      //没有用户id,走时间流刷新闻
      case _ => cid match {
        case 1L => newsNoUidService.loadQidian(page, newcount, tcursor, nid).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
        }
        case _ => newsNoUidService.loadFeedByChannel(cid, sechidOpt, page, newcount, tcursor, nid).map {
          case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
          case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
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

  def updateNewsFeedCommon() = Action.async { implicit request =>
    qidianService.updateNewsFeedCommon().map {
      case true => ServerSucced(true)
      case _    => ServerSucced(false)
    }
  }

}
