package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import play.api.libs.json.Json
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.news._
import services.users.UserService
import services.video.VideoService
import utils.Response._
import services.spiders.SourceService
import services.community.ASearchService

import scala.concurrent.{ ExecutionContext, Future }
import commons.utils.Base64Utils.decodeBase64
import commons.models.channels.{ ChannelResponse, ChannelRow }
import commons.models.community.{ ASearch, ASearchRow }
import commons.models.news._
import commons.models.spiders.SourceResponse
import commons.models.userprofiles.CommentResponse
import commons.models.users._
import org.joda.time.LocalDateTime
import utils.ResponseRecommand.{ DataEmptyError => _, DataInvalidError => _, ServerSucced => _, _ }

import scala.util.Random

/**
 * Created by zhange on 2016-05-16.
 *
 */

class NewsController @Inject() (val userService: UserService, val channelService: ChannelService, val newsService: NewsService, val videoService: VideoService,
                                val sourceService: SourceService, val commentService: CommentService, val aSearchService: ASearchService,
                                val newsPublisherService: NewsPublisherService, val pvdetailService: PvdetailService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def listChannel(state: Int, sech: Int) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(0, "NewsController.listChannel", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    channelService.listWithSeChannel(state, sech).map {
      case channels: Seq[ChannelResponse] if channels.nonEmpty => ServerSucced(channels)
      case _                                                   => DataEmptyError(s"$state")
    }
  }

  def listSource(page: Long, count: Long) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(0, "NewsController.listSource", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    sourceService.listByOnline(page = page, count = count).map {
      case sources: Seq[SourceResponse] if sources.nonEmpty => ServerSucced(sources)
      case _                                                => DataEmptyError(s"$page, $count")
    }
  }

  def getDetails(nid: Long, uid: Option[Long], s: Int) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid.getOrElse(0), "NewsController.getDetails", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    newsService.findDetailsWithProfileByNid(nid, uid).map {
      case Some(news) => ServerSucced(if (s == 1) https(news) else news)
      case _          => DataEmptyError(s"$nid")
    }
  }

  def getNextDetails(nid: Long, uid: Option[Long], s: Int, chid: Long) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid.getOrElse(0), "NewsController.getNextDetails", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    newsService.findNextDetailsWithProfileByNid(nid, uid, chid).map {
      case Some(news) => ServerSucced(if (s == 1) https(news) else news)
      case _          => DataEmptyError(s"$nid")
    }
  }

  def getLastDetails(nid: Long, uid: Option[Long], s: Int, chid: Long) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid.getOrElse(0), "NewsController.getLastDetails", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    newsService.findLastDetailsWithProfileByNid(nid, uid, chid).map {
      case Some(news) => ServerSucced(if (s == 1) https(news) else news)
      case _          => DataEmptyError(s"$nid")
    }
  }

  def getVideoDetails(nid: Long, uid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid.getOrElse(0), "NewsController.getVideoDetails", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    videoService.findDetailsWithProfileByNid(nid, uid).map {
      case Some(news) => ServerSucced(news)
      case _          => DataEmptyError(s"$nid")
    }
  }

  final private def https(detail: NewsDetailsResponse): NewsDetailsResponse = {
    var list: List[NewsBodyBlock] = detail.content.as[List[NewsBodyBlock]]
    list = list.map { news =>
      news match {
        case imageBlock: ImageBlock => imageBlock.copy(img = imageBlock.img.replace("http://pro-pic.deeporiginalx.com", "https://bdp-images.oss-cn-hangzhou.aliyuncs.com"))
        case _                      => news
      }
    }
    detail.copy(content = Json.toJson(list))
  }

  def listASearch(nid: Long, page: Long, count: Long, s: Int) = Action.async { implicit request =>
    //和详情页是同一个pv
    //pvdetailService.insert(PvDetail(0, "NewsController.listASearch", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    aSearchService.listByRefer(nid.toString, page, count).map {
      case searchs: Seq[ASearchRow] if searchs.nonEmpty => ServerSucced(if (s == 1) https(searchs.map(_.asearch)) else searchs.map(_.asearch))
      case _                                            => DataEmptyError(s"$nid")
    }
  }

  //AsyncStack(AuthorityKey -> GuestRole) { implicit request =>
  def loadFeed(uid: Option[Long], chid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid.getOrElse(0), "NewsController.loadFeed", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    chid match {
      case 1L => newsService.loadFeedByRecommends(page, count, tcursor, nid).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$chid, $page, $count, $tcursor")
      }
      case _ => newsService.loadFeedByChannel(uid.getOrElse(0), chid, sechidOpt, page, count, tcursor, nid).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$chid, $page, $count, $tcursor")
      }
    }
  }

  def refreshFeed(uid: Option[Long], chid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(uid.getOrElse(0), "NewsController.refreshFeed", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    chid match {
      case 1L => newsService.refreshFeedByRecommends(page, count, tcursor, nid).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$chid, $page, $count, $tcursor")
      }
      case _ => newsService.refreshFeedByChannel(uid.getOrElse(0), chid, sechidOpt, page, count, tcursor, nid).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$chid, $page, $count, $tcursor")
      }
    }
  }

  def loadLocationFeed(page: Long, count: Long, tcursor: Long, tmock: Int, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(0, "NewsController.loadLocationFeed", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    (province, city, district) match {
      case (None, None, None) => Future.successful(DataInvalidError("Not all location fields are empty"))
      case _ => newsService.loadFeedByLocation(page, count, tcursor, province, city, district, nid).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$page, $count, $tcursor, $province, $city, $district")
      }
    }
  }

  def refreshLocationFeed(page: Long, count: Long, tcursor: Long, tmock: Int, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(0, "NewsController.refreshLocationFeed", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    (province, city, district) match {
      case (None, None, None) => Future.successful(DataInvalidError("Not all location fields are empty"))
      case _ => newsService.refreshFeedByLocation(page, count, tcursor, province, city, district, nid).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$page, $count, $tcursor, $province, $city, $district")
      }
    }
  }

  def loadSourceFeed(sid: Long, page: Long, count: Long, tcursor: Long, tmock: Int, nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(0, "NewsController.loadSourceFeed", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    newsService.loadFeedBySource(sid, page, count, tcursor, nid).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
      case _                                            => DataEmptyError(s"$sid, $page, $count, $tcursor")
    }
  }

  def refreshSourceFeed(sid: Long, page: Long, count: Long, tcursor: Long, tmock: Int, nid: Option[Long]) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(0, "NewsController.refreshSourceFeed", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    newsService.refreshFeedBySource(sid, page, count, tcursor, nid).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
      case _                                            => DataEmptyError(s"$sid, $page, $count, $tcursor")
    }
  }

  final private def mockRealTime(news: Seq[NewsFeedResponse]): Seq[NewsFeedResponse] = {
    val mockIndexs = Random.shuffle((1 to news.length - 2).toList).slice(0, news.length / 4)
    news.map {
      case n: NewsFeedResponse if mockIndexs.contains(news.indexOf(n)) => n.copy(ptime = LocalDateTime.now().plusMinutes(-Random.nextInt(5)))
      case n: NewsFeedResponse                                         => n
    }
  }

  def listCommentCommon(did: String, uid: Option[Long], page: Long, count: Long, s: Int) = Action.async { implicit request =>
    uid match {
      case Some(uuid) =>
        commentService.listByDocidAndUid(decodeBase64(did), uuid, page, count).map {
          case cs: Seq[CommentResponse] if cs.nonEmpty => ServerSucced(if (s == 1) https1(cs) else cs)
          case _                                       => DataEmptyError(s"$did, $page, $count")
        }
      case None =>
        commentService.listByDocid(decodeBase64(did), page, count).map {
          case cs: Seq[CommentResponse] if cs.nonEmpty => ServerSucced(if (s == 1) https1(cs) else cs)
          case _                                       => DataEmptyError(s"$did, $page, $count")
        }
    }
  }

  def listCommentHot(did: String, uid: Option[Long], page: Long, count: Long, s: Int) = Action.async { implicit request =>
    uid match {
      case Some(uuid) => commentService.listByDocidAndUidHot(decodeBase64(did), uuid, page, count).map {
        case cs: Seq[CommentResponse] if cs.nonEmpty => ServerSucced(if (s == 1) https1(cs) else cs)
        case _                                       => DataEmptyError(s"$did, $uid, $page, $count")
      }
      case None => commentService.listByDocidHot(decodeBase64(did), page, count).map {
        case cs: Seq[CommentResponse] if cs.nonEmpty => ServerSucced(if (s == 1) https1(cs) else cs)
        case _                                       => DataEmptyError(s"$did, $uid, $page, $count")
      }
    }
  }

  def listNewsByPublisher(pname: String, page: Long, count: Long, infoFlag: Int, tcursor: Long) = Action.async { implicit request =>
    pvdetailService.insert(PvDetail(0, "NewsController.listNewsByPublisher", LocalDateTime.now(), request.headers.get("X-Real-IP")))
    newsPublisherService.listNewsByPublisher(pname, page, count, tcursor, infoFlag).map {
      case Right(newsFeedWithPublisherResponse) => ServerSucced(newsFeedWithPublisherResponse)
      case Left(exceptionMessage)               => ServerFailure(exceptionMessage)
    }
  }

  //http改https
  final private def https(aSearch: Seq[ASearch]): Seq[ASearch] = {
    aSearch.map { aSearch =>
      aSearch.img match {
        case Some(imag: String) => aSearch.copy(img = Some(https(imag)))
        case None               => aSearch
      }
    }
  }

  final private def https(imag: String): String = {
    imag.replace("http://pro-pic.deeporiginalx.com", "https://bdp-images.oss-cn-hangzhou.aliyuncs.com").replace("http://bdp-pic.deeporiginalx.com", "https://bdp-images.oss-cn-hangzhou.aliyuncs.com")
  }

  final private def https1(comment: Seq[CommentResponse]): Seq[CommentResponse] = {
    comment.map { comment =>
      comment.avatar match {
        case Some(imag: String) => comment.copy(avatar = Some(https1(imag)))
        case None               => comment
      }
    }
  }

  final private def https1(imag: String): String = {
    if (imag.indexOf("http://pro-pic.deeporiginalx.com") == 0 || imag.indexOf("http://bdp-pic.deeporiginalx.com") == 0)
      imag.replace("http://pro-pic.deeporiginalx.com", "https://bdp-images.oss-cn-hangzhou.aliyuncs.com").replace("http://bdp-pic.deeporiginalx.com", "https://bdp-images.oss-cn-hangzhou.aliyuncs.com")
    else
      imag.replace("http", "https")
  }
}
