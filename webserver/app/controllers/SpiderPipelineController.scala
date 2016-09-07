package controllers

import javax.inject._

import akka.actor._
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import commons.messages.pipeline.NewsPipelineTask
import commons.models.community.ASearchRow
import commons.models.news.{NewsPipelineWithKey, NewsRow}
import commons.models.userprofiles.CommentRow
import commons.utils.Base64Utils.decodeBase64
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc._
import services.community.ASearchService
import services.news.NewsService
import services.userprofiles.ProfileService
import utils.Response._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.NonFatal
/**
 * Created by zhange on 2016-05-19.
 *
 */

@Singleton
class SpiderPipelineController @Inject() (system: ActorSystem, val profileService: ProfileService,
                                          val asearchService: ASearchService, val newsService: NewsService) extends Controller {

  import system.dispatcher
  implicit val timeout: Timeout = 15.seconds
  val newsPipelineRoutees: ActorRef = system.actorOf(FromConfig.props(), "NewsPipelineRoutees")

  def newsPipelineTest(msg: String) = Action.async {
    (newsPipelineRoutees ? msg).map {
      case msg: String => ServerSucced(msg)
    }.recover {
      case NonFatal(e) => ServerError(s"NewsPipelineError: ${e.getMessage}")
    }
  }

  def newsPipeline() = Action.async(parse.json) { implicit request =>
    request.body.validate[NewsPipelineWithKey] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(newsPipelineWithKey, _) =>
        (newsPipelineRoutees ? NewsPipelineTask(decodeBase64(newsPipelineWithKey.key))).map {
          case msg: String         => ServerError(msg)
          case NewsPipelineTask(t) => ServerSucced(t)
        }.recover {
          case NonFatal(e) => ServerError(s"NewsPipelineError: ${e.getMessage}")
        }
    }
  }

  def createComments() = Action.async(parse.json) { implicit request =>
    request.body.validate[CommentRow] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(commentRow, _) => profileService.addComment(commentRow).map {
        case Some(comment) => ServerSucced(comment)
        case _             => DataEmptyError(s"${commentRow.toString}")
      }
    }
  }

  def createNews() = Action.async(parse.json) { implicit request =>
    request.body.validate[NewsRow] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(newsRow, _) => newsService.insert(newsRow).map {
        case Some(url) => ServerSucced(url)
        case _         => DataEmptyError(s"${newsRow.toString}")
      }
    }
  }

  def createSearchItems() = Action.async(parse.json) { implicit request =>
    request.body.validate[List[ASearchRow]] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(asearchRows, _) => asearchService.insertMulti(asearchRows).map {
        case ids: Seq[Long] if ids.nonEmpty => ServerSucced(ids)
        case ids: Seq[Long]                 => DataEmptyError(s"${ids.toString}")
      }
    }
  }
}
