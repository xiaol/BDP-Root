package controllers

import javax.inject.Inject

import play.api.mvc._
import services.news._
import utils.ResponseRecommand._

import scala.concurrent.ExecutionContext
import commons.models.news.{ NewsFeedResponse, NewsRecommendResponse }

class NewsSearchController @Inject() (val newsEsService: NewsEsService, val newsCacheService: NewsCacheService, val newsRecommendService: NewsRecommendService)(implicit ec: ExecutionContext)
    extends Controller {

  def search(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int) = Action.async { implicit request =>
    newsEsService.search(key, pname, channel, page, count).map {
      case news: (Seq[NewsFeedResponse], Long) if news._1.nonEmpty => ServerSucced(news._1, Some(news._2))
      case _                                                       => DataEmptyError(s"$key")
    }
  }

  def recommend(uid: Long, count: Int) = Action.async { implicit request =>
    newsCacheService.getNewRowList(uid, count).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$uid")
    }
  }

  def addRecommend(nid: Long, score: Double) = Action.async { implicit request =>
    newsCacheService.addRecommendNew(nid, score).map {
      case true => ServerSucced(true)
      case _    => DataEmptyError(s"$nid")
    }
  }

  def operate(nid: Long, method: String, level: Option[Double], bigimg: Option[Int]) = Action.async { implicit request =>
    newsRecommendService.operate(nid: Long, method: String, level: Option[Double], bigimg: Option[Int]).map {
      case Some(l) => ServerSucced(l)
      case _       => DataEmptyError(s"$nid")
    }
  }

  def listNewsByRecommand(channel: Long, ifrecommend: Int, page: Long, count: Long) = Action.async { implicit request =>
    newsRecommendService.listNewsAndCountByRecommand(channel: Long, ifrecommend: Int, page: Long, count: Long).map {
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
