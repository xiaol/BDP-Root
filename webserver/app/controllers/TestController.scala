package controllers

import javax.inject.Inject

import commons.models.news._
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.newsfeed._
import services.test.{ Test, TestService }
import services.users.UserService
import utils.Response._
import utils.ResponseRecommand.{ DataEmptyError => _, DataInvalidError => _, ServerSucced => _ }

import scala.concurrent.ExecutionContext
import scala.util.Random

/**
 * Created by zhange on 2016-05-16.
 *
 */
class TestController @Inject() (val userService: UserService, val qidianService: QidianNewsWithUserCacheService, val testService: TestService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def select1(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    testService.select1(Random.nextInt(23225910) + 132177, page, count, tcursor, t, None, None, request.headers.get("X-Real-IP")).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
    }
  }

  def select2(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    testService.select2(Random.nextInt(23225910) + 132177, page, count, tcursor, t, None, None, request.headers.get("X-Real-IP")).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
    }
  }

  def select3(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    testService.select3(Random.nextInt(23225910) + 132177, page, count, tcursor, t, None, None, request.headers.get("X-Real-IP")).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
    }
  }

  def select4(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    testService.select4(Random.nextInt(23225910) + 132177, page, count, tcursor, t, None, None, request.headers.get("X-Real-IP")).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
    }
  }

  def select5(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    testService.select5(Random.nextInt(23225910) + 132177, page, count, tcursor, t, None, None, request.headers.get("X-Real-IP")).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
    }
  }

  def select6(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    testService.select6(Random.nextInt(23225910) + 132177, page, count, tcursor, t, None, None, request.headers.get("X-Real-IP")).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
    }
  }

  def select7(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    testService.select7(Random.nextInt(23225910) + 132177, page, count, tcursor, t, None, None, request.headers.get("X-Real-IP")).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
    }
  }

  def test(cid: Long, sechidOpt: Option[Long], page: Long, count: Long, tcursor: Long, tmock: Int, uid: Long, t: Int, nid: Option[Long]) = Action.async { implicit request =>
    testService.test(uid, page, 9, tcursor, t, None, None, request.headers.get("X-Real-IP")).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$cid, $page, $count, $tcursor")
    }
  }

  def testNewsFeed() = Action.async { implicit request =>
    testService.testNewsFeed().map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError("")
    }
  }

  def testToBean() = Action.async { implicit request =>
    testService.testToBean().map {
      case news: Seq[NewsFeedRow] if news.nonEmpty => ServerSucced(news)
      case _                                       => DataEmptyError("")
    }
  }

}
