package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc.Controller
import play.api.mvc.Action
import security.auth.AuthConfigImpl
import services.news._
import services.spiders.{ QueueService, SourceService }
import services.users.UserService
import commons.models.spiders.SourceRow
import commons.models.spiders.QueueRow
import utils.Response._
import play.api.libs.json.{ JsError, JsSuccess }

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * Created by zhange on 2016-05-17.
 *
 */

class SpiderController @Inject() (val sourceService: SourceService, val queueService: QueueService)(implicit ec: ExecutionContext)
    extends Controller {

  def listQueue(page: Long, count: Long) = Action.async { implicit request =>
    queueService.list(page, count).map {
      case queues: Seq[QueueRow] if queues.nonEmpty => ServerSucced(queues)
      case _                                        => DataEmptyError(s"$page, $count")
    }
  }

  def createQueue() = Action.async(parse.json) { implicit request =>
    request.body.validate[QueueRow] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(queueRow, _) => queueService.insert(queueRow).map {
        case Some(q) => ServerSucced(q)
        case _       => DataCreateError(s"${queueRow.toString}")
      }
    }
  }

  def updateQueue(queue: String) = Action.async(parse.json) { implicit request =>
    request.body.validate[QueueRow] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(queueRow, _) => queueService.update(queue, queueRow).map {
        case Some(queueRowNew) => ServerSucced(queueRowNew)
        case _                 => DataCreateError(s"$queue, ${queueRow.toString}")
      }
    }
  }

  def deleteQueue(queue: String) = Action.async { implicit request =>
    queueService.delete(queue).map {
      case Some(q) => ServerSucced(q)
      case _       => DataDeleteError(s"$queue")
    }
  }

  def listSource(state: Int, status: Int, page: Long, count: Long) = Action.async { implicit request =>
    sourceService.listByState(state, status, page, count).map {
      case sources: Seq[SourceRow] if sources.nonEmpty => ServerSucced(sources)
      case _                                           => DataEmptyError(s"$state, $status, $page, $count")
    }
  }

  def listSourceByQueue(queue: String, page: Long, count: Long) = Action.async { implicit request =>
    sourceService.listByQueue(queue, page, count).map {
      case sources: Seq[SourceRow] if sources.nonEmpty => ServerSucced(sources)
      case _                                           => DataEmptyError(s"$queue, $page, $count")
    }
  }

  def createSource() = Action.async(parse.json) { implicit request =>
    request.body.validate[SourceRow] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(sourceRow, _) => sourceService.insert(sourceRow).map {
        case Some(q) => ServerSucced(q)
        case _       => DataCreateError(s"${sourceRow.toString}")
      }
    }
  }

  def updateSource(sid: Long) = Action.async(parse.json) { implicit request =>
    request.body.validate[SourceRow] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(sourceRow, _) => sourceService.update(sid, sourceRow).map {
        case Some(sourceRowUpdated) => ServerSucced(sourceRowUpdated)
        case _                      => DataCreateError(s"$sid, ${sourceRow.toString}")
      }
    }
  }

  def deleteSource(sid: Long) = Action.async { implicit request =>
    sourceService.delete(sid).map {
      case Some(id) => ServerSucced(id)
      case _        => DataDeleteError(s"$sid")
    }
  }
}
