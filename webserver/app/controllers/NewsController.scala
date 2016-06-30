package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.news._
import services.users.UserService
import utils.Response._
import services.spiders.SourceService
import services.community.ASearchService

import scala.concurrent.{ ExecutionContext, Future }
import commons.utils.Base64Utils.decodeBase64
import commons.models.channels.ChannelRow
import commons.models.community.ASearchRow
import commons.models.news.NewsFeedResponse
import commons.models.spiders.SourceResponse
import commons.models.userprofiles.CommentResponse
import commons.models.users._
import org.joda.time.LocalDateTime

import scala.util.Random

/**
 * Created by zhange on 2016-05-16.
 *
 */

class NewsController @Inject() (val userService: UserService, val channelService: ChannelService, val newsService: NewsService,
                                val sourceService: SourceService, val commentService: CommentService, val aSearchService: ASearchService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def listChannel(state: Int) = Action.async { implicit request =>
    channelService.list(state).map {
      case channels: Seq[ChannelRow] if channels.nonEmpty => ServerSucced(channels)
      case _                                              => DataEmptyError(s"$state")
    }
  }

  def listSource(page: Long, count: Long) = Action.async { implicit request =>
    sourceService.listByOnline(page = page, count = count).map {
      case sources: Seq[SourceResponse] if sources.nonEmpty => ServerSucced(sources)
      case _                                                => DataEmptyError(s"$page, $count")
    }
  }

  def getDetails(nid: Long) = Action.async { implicit request =>
    newsService.findDetailsByNid(nid).map {
      case Some(news) => ServerSucced(news)
      case _          => DataEmptyError(s"$nid")
    }
  }

  def listASearch(nid: Long, page: Long, count: Long) = Action.async { implicit request =>
    aSearchService.listByRefer(nid.toString, page, count).map {
      case searchs: Seq[ASearchRow] if searchs.nonEmpty => ServerSucced(searchs.map(_.asearch))
      case _                                            => DataEmptyError(s"$nid")
    }
  }

  // def getRelated(nid: String) = ???

  def loadFeed(cid: Long, page: Long, count: Long, tcursor: Long, tmock: Int) = AsyncStack(AuthorityKey -> GuestRole) { implicit request =>
    cid match {
      case 1L => newsService.loadFeedByRecommends(page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
      case _ => newsService.loadFeedByChannel(cid, page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
    }
  }

  def refreshFeed(cid: Long, page: Long, count: Long, tcursor: Long, tmock: Int) = AsyncStack(AuthorityKey -> GuestRole) { implicit request =>
    cid match {
      case 1L => newsService.refreshFeedByRecommends(page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
      case _ => newsService.refreshFeedByChannel(cid, page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
    }
  }

  def loadLocationFeed(page: Long, count: Long, tcursor: Long, tmock: Int, province: Option[String], city: Option[String], district: Option[String]) = AsyncStack(AuthorityKey -> GuestRole) { implicit request =>
    (province, city, district) match {
      case (None, None, None) => Future.successful(DataInvalidError("Not all location fields are empty"))
      case _ => newsService.loadFeedByLocation(page, count, tcursor, province, city, district).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$page, $count, $tcursor, $province, $city, $district")
      }
    }
  }

  def refreshLocationFeed(page: Long, count: Long, tcursor: Long, tmock: Int, province: Option[String], city: Option[String], district: Option[String]) = AsyncStack(AuthorityKey -> GuestRole) { implicit request =>
    (province, city, district) match {
      case (None, None, None) => Future.successful(DataInvalidError("Not all location fields are empty"))
      case _ => newsService.refreshFeedByLocation(page, count, tcursor, province, city, district).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$page, $count, $tcursor, $province, $city, $district")
      }
    }
  }

  def loadSourceFeed(sid: Long, page: Long, count: Long, tcursor: Long, tmock: Int) = Action.async { implicit request =>
    newsService.loadFeedBySource(sid, page, count, tcursor).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
      case _                                            => DataEmptyError(s"$sid, $page, $count, $tcursor")
    }
  }

  def refreshSourceFeed(sid: Long, page: Long, count: Long, tcursor: Long, tmock: Int) = Action.async { implicit request =>
    newsService.refreshFeedBySource(sid, page, count, tcursor).map {
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

  def listCommentCommon(did: String, uid: Option[Long], page: Long, count: Long) = Action.async { implicit request =>
    uid match {
      case Some(uuid) =>
        commentService.listByDocidAndUid(decodeBase64(did), uuid, page, count).map {
          case cs: Seq[CommentResponse] if cs.nonEmpty => ServerSucced(cs)
          case _                                       => DataEmptyError(s"$did, $page, $count")
        }
      case None =>
        commentService.listByDocid(decodeBase64(did), page, count).map {
          case cs: Seq[CommentResponse] if cs.nonEmpty => ServerSucced(cs)
          case _                                       => DataEmptyError(s"$did, $page, $count")
        }
    }
  }

  def listCommentHot(did: String, uid: Option[Long], page: Long, count: Long) = Action.async { implicit request =>
    uid match {
      case Some(uuid) => commentService.listByDocidAndUidHot(decodeBase64(did), uuid, page, count).map {
        case cs: Seq[CommentResponse] if cs.nonEmpty => ServerSucced(cs)
        case _                                       => DataEmptyError(s"$did, $uid, $page, $count")
      }
      case None => commentService.listByDocidHot(decodeBase64(did), page, count).map {
        case cs: Seq[CommentResponse] if cs.nonEmpty => ServerSucced(cs)
        case _                                       => DataEmptyError(s"$did, $uid, $page, $count")
      }
    }
  }
}
