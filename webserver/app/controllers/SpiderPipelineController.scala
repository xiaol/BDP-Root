package controllers

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import play.api.mvc._
import akka.actor._
import javax.inject._

import akka.routing.FromConfig
import utils.Response._
import services.news.NewsService
import play.api.libs.json.{ JsError, JsSuccess }
import services.community.ASearchService

import scala.concurrent.Future
import scala.util.control.NonFatal
import commons.utils.Base64Utils.decodeBase64
import commons.models.community.ASearchRow
import commons.models.news.NewsRow
import commons.models.userprofiles.CommentRow
import commons.messages.pipeline.NewsPipelineTask
import services.userprofiles.ProfileService

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

  def newsPipeline(task: String) = Action.async {
    (newsPipelineRoutees ? NewsPipelineTask(decodeBase64(task))).map {
      case msg: String         => ServerError(msg)
      case NewsPipelineTask(t) => ServerSucced(t)
    }.recover {
      case NonFatal(e) => ServerError(s"NewsPipelineError: ${e.getMessage}")
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
