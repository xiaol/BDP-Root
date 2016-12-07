package controllers

import javax.inject.Inject

import commons.models.news._
import commons.models.userprofiles.Searchnewslist
import org.joda.time.LocalDateTime
import play.api.mvc._
import services.news._
import services.userprofiles.ProfileService
import utils.ResponseRecommand._

import scala.concurrent.{ ExecutionContext, Future }

class NewsSearchController @Inject() (val newsEsService: NewsEsService, val newsRecommendService: NewsRecommendService, val profileService: ProfileService)(implicit ec: ExecutionContext)
    extends Controller {

  //正常搜索新闻
  def search(key: String, pname: Option[String], channel: Option[Long], uid: Option[Long], page: Int, count: Int) = Action.async { implicit request =>

    //记录搜索历史
    uid match {
      case Some(uid) => profileService.addSearch(Searchnewslist(None, uid, key, Some(LocalDateTime.now())))
      case _         =>
    }

    //搜索结果
    newsEsService.search(key, pname, channel, page, count).map {
      case news: (Seq[NewsFeedResponse], Long) if news._1.nonEmpty => ServerSucced(news._1, Some(news._2))
      case _                                                       => DataEmptyError(s"$key")
    }
  }

  //根据百度热词,搜索热点新闻
  def searchHotNid(key: String, page: Int, count: Int) = Action.async { implicit request =>
    newsEsService.searchHotNid(key, page, count).map {
      case nid: Long if nid != 0L => ServerSucced(nid)
      case _                      => DataEmptyError(s"$key")
    }
  }

  //搜索新闻并添加订阅号列表及用户是否关注该订阅号
  def searchNewsWithPublisher(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int, uid: Option[Long]) = Action.async { implicit request =>
    //记录搜索历史
    uid match {
      case Some(uid) => profileService.addSearch(Searchnewslist(None, uid, key, Some(LocalDateTime.now())))
      case _         =>
    }
    if (page == 1) {
      val news: Future[(Seq[NewsFeedResponse], Long)] = newsEsService.search(key, pname, channel, page, count)
      val publisher: Future[Seq[(NewsPublisherRow, Long)]] = newsRecommendService.listPublisherWithFlag(uid, key)
      val t: Future[NewsFeedWithPublisherWithUserInfoResponse] = for {
        news <- news
        publisher <- publisher.map { seq =>
          seq.map { p =>
            NewsPublisherWithUserResponse(p._1.id, p._1.ctime, p._1.name, p._1.icon, p._1.descr, p._1.concern, p._2)
          }
        }
      } yield (NewsFeedWithPublisherWithUserInfoResponse(news._1, news._2, Some(publisher)))
      t.map {
        case r: NewsFeedWithPublisherWithUserInfoResponse if r.news.nonEmpty => ServerSucced(r)
        case _                                                               => DataEmptyError(s"$key")
      }
    } else {
      val news: Future[(Seq[NewsFeedResponse], Long)] = newsEsService.search(key, pname, channel, page, count)
      val t: Future[NewsFeedWithPublisherWithUserInfoResponse] = for {
        news <- news
      } yield (NewsFeedWithPublisherWithUserInfoResponse(news._1, news._2, None))
      t.map {
        case r: NewsFeedWithPublisherWithUserInfoResponse if r.news.nonEmpty => ServerSucced(r)
        case _                                                               => DataEmptyError(s"$key")
      }
    }

  }

  def operate(nid: Long, method: String, level: Option[Double], bigimg: Option[Int]) = Action.async { implicit request =>
    newsRecommendService.operate(nid: Long, method: String, level: Option[Double], bigimg: Option[Int]).map {
      case Some(l) => ServerSucced(l)
      case _       => DataEmptyError(s"$nid")
    }
  }

  def listNewsByRecommand(channel: Option[Long], ifrecommend: Int, page: Long, count: Long) = Action.async { implicit request =>
    newsRecommendService.listNewsAndCountByRecommand(channel: Option[Long], ifrecommend: Int, page: Long, count: Long).map {
      case news: (Seq[NewsRecommendResponse], Long) if news._1.nonEmpty => ServerSucced(news._1, Some(news._2))
      case _                                                            => DataEmptyError(s"$channel,$ifrecommend")
    }
  }

  def listNewsBySearch(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int) = Action.async { implicit request =>
    newsRecommendService.listNewsBySearch(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int).map {
      case news: (Seq[NewsRecommendResponse], Long) if news._1.nonEmpty => ServerSucced(news._1, Some(news._2))
      case _                                                            => DataEmptyError(s"$key,$pname,$channel")
    }
  }
}
