package controllers

import javax.inject.Inject

import commons.models.news.NewsFeedResponse
import commons.models.users._
import jp.t2v.lab.play2.auth.AuthElement
import org.joda.time.LocalDateTime
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.news._
import services.users.UserService
import utils.Response._
import utils.ResponseRecommand.{ DataEmptyError => _, DataInvalidError => _, ServerSucced => _ }

import scala.concurrent.ExecutionContext
import scala.util.Random

/**
 * Created by zhangshl on 16/7/27.
 */
class NewsRecommendController @Inject() (val userService: UserService, val newsRecommendService: NewsRecommendService, val newsService: NewsService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def loadFeedNew(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long) = Action.async { implicit request =>
    cid match {
      case 1L => newsRecommendService.loadFeedByRecommendsNew(uid, page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
      case _ => newsService.loadFeedByChannel(cid, sechidOpt, page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
    }
  }

  def refreshFeedNew(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long) = AsyncStack(AuthorityKey -> GuestRole) { implicit request =>
    cid match {
      case 1L => newsRecommendService.refreshFeedByRecommendsNew(uid, page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
      }
      case _ => newsService.refreshFeedByChannel(cid, sechidOpt, page, count, tcursor).map {
        case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(if (1 == tmock) mockRealTime(news) else news)
        case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
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