package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import play.api.mvc._
import akka.actor._
import javax.inject._

import actors.SpiderDispatcherServer
import commons.messages.dispatcher._
import services.spiders.SourceService
import utils.Response._
import utils.RedisDriver.cache
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scala.util.control.NonFatal
import actors.TestActor
import commons.models.spiders.SourceRow

/**
 * Created by zhange on 2016-05-18.
 *
 */

@Singleton
class SpiderDispatcherController @Inject() (system: ActorSystem, val sourceService: SourceService)
    extends Controller {

  implicit val timeout: Timeout = 15.seconds
  val dispatcher = system.actorOf(SpiderDispatcherServer.props)

  // TODO: create a service as remote to receice insert task from pipeline server
  val testReceiveAsRemote = system.actorOf(TestActor.props, "PlayAsRemote")

  def testDispatcher(msg: String) = Action.async {
    import commons.messages.dispatcher.DispatchTest
    (dispatcher ? DispatchTest(msg)).map {
      case msg: DispatcherMessage => ServerSucced(msg.toString)
      case msg: String            => ServerError(msg)
    }.recover {
      case NonFatal(e) => ServerError(s"SpiderDispatcherError: ${e.getMessage}")
    }
  }

  def startAll() = Action.async { implicit request =>
    sourceService.listAll().flatMap {
      case sources: Seq[SourceRow] if sources.nonEmpty =>
        val dispatcherInits: StartDispatchers = StartDispatchers(sources.filter(_.status == 1).map(s => DispatcherInit(s)).toList)
        (dispatcher ? dispatcherInits).map {
          case msg: DispatchResponses => ServerSucced(msg.results)
          case msg: String            => ServerError(msg)
        }.recover {
          case NonFatal(e) => ServerError(s"SpiderDispatcherError: ${e.getMessage}")
        }
      case _ => Future.successful(DataEmptyError(""))
    }
  }

  def closeAll() = Action.async { implicit request =>
    sourceService.listAll().flatMap {
      case sources: Seq[SourceRow] if sources.nonEmpty =>
        val dispatcherInits: CloseDispatchers = CloseDispatchers(sources.filter(_.status == 1).map(s => DispatcherInit(s)).toList)
        (dispatcher ? dispatcherInits).map {
          case msg: DispatchResponses => ServerSucced(msg.results)
          case msg: String            => ServerError(msg)
        }.recover {
          case NonFatal(e) => ServerError(s"SpiderDispatcherError: ${e.getMessage}")
        }
      case _ => Future.successful(DataEmptyError(""))
    }
  }

  def reloadAll() = Action.async { implicit request =>
    sourceService.listAll().flatMap {
      case sources: Seq[SourceRow] if sources.nonEmpty =>
        val dispatcherInits: ReloadDispatchers = ReloadDispatchers(sources.filter(_.status == 1).map(s => DispatcherInit(s)).toList)
        (dispatcher ? dispatcherInits).map {
          case msg: DispatchResponses => ServerSucced(msg.results)
          case msg: String            => ServerError(msg)
        }.recover {
          case NonFatal(e) => ServerError(s"SpiderDispatcherError: ${e.getMessage}")
        }
      case _ => Future.successful(DataEmptyError(""))
    }
  }

  def startOne(sid: Long) = Action.async { implicit request =>
    sourceService.findById(sid).flatMap {
      case Some(source) =>
        val dispatcherInit: StartDispatcher = StartDispatcher(DispatcherInit(source))
        (dispatcher ? dispatcherInit).map {
          case msg: DispatchResponse => ServerSucced(msg)
          case msg: String           => ServerError(msg)
        }.recover {
          case NonFatal(e) => ServerError(s"SpiderDispatcherError: ${e.getMessage}")
        }
      case _ => Future.successful(DataEmptyError(""))
    }
  }

  def closeOne(sid: Long) = Action.async { implicit request =>
    sourceService.findById(sid).flatMap {
      case Some(source) =>
        val dispatcherInit: CloseDispatcher = CloseDispatcher(DispatcherInit(source))
        (dispatcher ? dispatcherInit).map {
          case msg: DispatchResponse => ServerSucced(msg)
          case msg: String           => ServerError(msg)
        }.recover {
          case NonFatal(e) => ServerError(s"SpiderDispatcherError: ${e.getMessage}")
        }
      case _ => Future.successful(DataEmptyError(""))
    }
  }

  def reloadOne(sid: Long) = Action.async { implicit request =>
    sourceService.findById(sid).flatMap {
      case Some(source) =>
        val dispatcherInit: ReloadDispatcher = ReloadDispatcher(DispatcherInit(source))
        (dispatcher ? dispatcherInit).map {
          case msg: DispatchResponse => ServerSucced(msg)
          case msg: String           => ServerError(msg)
        }.recover {
          case NonFatal(e) => ServerError(s"SpiderDispatcherError: ${e.getMessage}")
        }
      case _ => Future.successful(DataEmptyError(""))
    }
  }

  def pushSource(sid: Long) = Action.async { implicit request =>
    sourceService.findById(sid).flatMap {
      case Some(source) =>
        val init: ReloadDispatcher = ReloadDispatcher(DispatcherInit(source))
        cache.lpush(init.init.queue, Json.toJson(init.init.task).toString).onComplete {
          case Success(_) =>
          case Failure(err) =>
            Logger.error(s"LPUSH source failed: ${init.init.queue}, ${Json.toJson(init.init.task).toString} with err: ${err.getMessage}")
        }
        Future.successful(ServerSucced(DispatchResponse(init.init)))
      case _ => Future.successful(DataEmptyError(""))
    }
  }
}
