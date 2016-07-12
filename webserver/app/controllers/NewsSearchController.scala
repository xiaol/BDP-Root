package controllers

import javax.inject.Inject

import play.api.mvc._
import services.news._
import utils.Response._

import scala.concurrent.ExecutionContext
import commons.models.news.NewsFeedResponse

class NewsSearchController @Inject() (val newsEsService: NewsEsService, val newsCacheService: NewsCacheService)(implicit ec: ExecutionContext)
    extends Controller {

  def search(key: String, page: Int, count: Int) = Action.async { implicit request =>
    newsEsService.search(key, page, count).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$key")
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

}
