package controllers

import javax.inject.Inject

import commons.models.hottopic.HotNews
import org.joda.time.LocalDateTime
import play.api.http.ContentTypes
import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }
import services.hottopic.{ HotTopicService, HotWordsCacheService }
import services.news.NewsEsService
import utils.Response._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.parsing.json.{ JSONArray, JSONObject }

/**
 * Created by fengjigang on 17/4/19.
 * 热点新闻和热词控制器
 */
class HotTopicController @Inject() (val esService: NewsEsService, val hotTopicService: HotTopicService, val hotWordsCacheService: HotWordsCacheService)(implicit ec: ExecutionContext) extends Controller {

  def processHotNews = Action.async(parse.tolerantFormUrlEncoded) {
    request =>
      request.contentType match {
        case Some(ContentTypes.FORM) =>
          request.body.get("news") match {
            case Some(params: Seq[String]) if params.size > 0 =>
              val newsList = for (keyword <- params) yield {
                esService.searchHotNid(keyword, 1, 1).map { re => hotTopicService.insert(HotNews(re, LocalDateTime.now(), 1, keyword)) }
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

  def processCrawlerHotWords = Action.async(parse.tolerantFormUrlEncoded) { request =>
    request.contentType match {
      case Some(ContentTypes.FORM) =>
        request.body.get("words") match {
          case Some(params: Seq[String]) if params.size > 0 =>
            var wordsJson = params.map { title => JSONObject(Map("title" -> title, "baiduHotWord" -> new JSONArray(List()), "news_url" -> "")) }
            hotWordsCacheService.setHotWordsCache(new JSONArray(wordsJson.toList).toString())
            Future.successful(ServerSucced("Upload Hot Words Success"))
          case _ => Future.successful(ParamsInvalidError())
        }
      case _ => Future.successful(ContentTypeError())
    }
  }

  def getHotWords4Get = Action.async { request =>
    hotWordsCacheService.getHotWordsCache().map {
      t => ServerSucceedWithNoCode(Json.parse(t.getOrElse("[]")))
    }
  }

  def getHotWords4Post = Action.async { request =>
    hotWordsCacheService.getHotWordsCache().map {
      t => ServerSucceedWithNoCode(Json.parse(t.getOrElse("[]")))
    }
  }

}
