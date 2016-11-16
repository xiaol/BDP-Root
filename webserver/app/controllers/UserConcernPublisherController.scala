package controllers

import javax.inject.{ Inject, Singleton }

import commons.models.users.RegistRole
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.users.UserService
import utils.Response._

import scala.concurrent.ExecutionContext
import services.userprofiles.UserConcernPublisherService

/**
 * Created by zhange on 2016-07-15.
 *
 */

@Singleton
class UserConcernPublisherController @Inject() (val userConcernPublisherService: UserConcernPublisherService, val userService: UserService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def addConcernPublisher(uid: Long, pname: String) = Action.async { implicit request =>
    userConcernPublisherService.addConcernPublisher(uid, pname).map {
      case Right(updatedConcernCount) => ServerSucced(updatedConcernCount)
      case Left(exceptionMessage)     => ServerFailure(exceptionMessage)
    }
  }

  def remConcernPublisher(uid: Long, pname: String) = Action.async { implicit request =>
    userConcernPublisherService.remConcernPublisher(uid, pname).map {
      case Right(updatedConcernCount) => ServerSucced(updatedConcernCount)
      case Left(exceptionMessage)     => ServerFailure(exceptionMessage)
    }
  }

  def listConcernPublisher(uid: Long, page: Long, count: Long) = Action.async { implicit request =>
    userConcernPublisherService.listConcernPublisher(uid, page, count).map {
      case Right(concernPublisherSeq) => ServerSucced(concernPublisherSeq)
      case Left(exceptionMessage)     => ServerFailure(exceptionMessage)
    }
  }

  def loadNewsByConcernedPublishers(uid: Long, page: Long, count: Long, tcursor: Long) = Action.async { implicit request =>
    userConcernPublisherService.loadNewsByConcernedPublishers(uid, page, count, tcursor).map {
      case Right(newsFeedResponses) => ServerSucced(newsFeedResponses)
      case Left(exceptionMessage)   => ServerFailure(exceptionMessage)
    }
  }

  def refreshNewsByConcernedPublishers(uid: Long, page: Long, count: Long, tcursor: Long) = Action.async { implicit request =>
    userConcernPublisherService.refreshNewsByConcernedPublishers(uid, page, count, tcursor).map {
      case Right(newsFeedResponses) => ServerSucced(newsFeedResponses)
      case Left(exceptionMessage)   => ServerFailure(exceptionMessage)
    }
  }
}
