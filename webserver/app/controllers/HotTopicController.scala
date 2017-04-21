package controllers

import javax.inject.Inject

import commons.models.hottopic.HotNews
import org.joda.time.LocalDateTime
import play.api.http.ContentTypes
import play.api.mvc.{ Action, Controller }
import services.hottopic.HotTopicService
import services.news.NewsEsService
import utils.Response._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by fengjigang on 17/4/19.
 * 热点新闻和热词控制器
 */
class HotTopicController @Inject() (var esService: NewsEsService, var hotTopicService: HotTopicService)(implicit ec: ExecutionContext) extends Controller {

  def processHotNews = Action.async(parse.tolerantFormUrlEncoded) {
    request =>
      request.contentType match {
        case Some(ContentTypes.FORM) =>
          request.body.get("news") match {
            case Some(params: Seq[String]) if params.size > 0 =>
              val newsList = for (keyword <- params) yield {
                esService.searchHotNid(keyword, 1, 1).map { re => hotTopicService.insert(HotNews(re, LocalDateTime.now(), 1, keyword)) }
                //                Future.successful(Random.nextLong()).map { re => hotTopicService.insert(HotNews(re, LocalDateTime.now(), 1, keyword)) }
              }
              //              val resultList = Future.sequence(newsList).map {
              //                news =>
              //                  hotTopicService.insertAll(news)
              //              }
              Future.successful(ServerSucced("Upload Hot News Success"))
            case _ =>
              Future.successful(ParamsInvalidError())
          }
        case _ => Future.successful(ContentTypeError())
      }

  }

}
