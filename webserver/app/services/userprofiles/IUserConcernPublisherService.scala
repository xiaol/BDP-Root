package services.userprofiles

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import commons.utils.JodaUtils._
import commons.utils._
import dao.userprofiles._
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-07-15.
 *
 */

@ImplementedBy(classOf[UserConcernPublisherService])
trait IUserConcernPublisherService {
  def addConcernPublisher(uid: Long, pname: String): Future[Either[DBExceptionMessage, Int]]
  def remConcernPublisher(uid: Long, pname: String): Future[Either[DBExceptionMessage, Int]]
  def listConcernPublisher(uid: Long, page: Long, count: Long): Future[Either[DBExceptionMessage, Seq[NewsPublisherRow]]]
  def loadNewsByConcernedPublishers(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Either[DBExceptionMessage, Seq[NewsFeedResponse]]]
  def refreshNewsByConcernedPublishers(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Either[DBExceptionMessage, Seq[NewsFeedResponse]]]
}

class UserConcernPublisherService @Inject() (val concernPublisherDAO: ConcernPublisherDAO) extends IUserConcernPublisherService {

  def addConcernPublisher(uid: Long, pname: String): Future[Either[DBExceptionMessage, Int]] = {
    concernPublisherDAO.insertAndUpdateNewsPublisherConcerns(uid, pname).map(Right(_)).recover {
      case e: PGDBException => Left(e.getErrorEntity)
      case NonFatal(e) =>
        Logger.error(s"Within UserConcernPublisherService.addConcernPublisher($uid, $pname): ${e.getMessage}")
        Left(ExecutionFail(e.getMessage))
    }
  }

  def remConcernPublisher(uid: Long, pname: String): Future[Either[DBExceptionMessage, Int]] = {
    concernPublisherDAO.deleteAndUpdateNewsPublisherConcerns(uid, pname).map(Right(_)).recover {
      case e: PGDBException => Left(e.getErrorEntity)
      case NonFatal(e) =>
        Logger.error(s"Within UserConcernPublisherService.remConcernPublisher($uid, $pname): ${e.getMessage}")
        Left(ExecutionFail(e.getMessage))
    }
  }

  def listConcernPublisher(uid: Long, page: Long, count: Long): Future[Either[DBExceptionMessage, Seq[NewsPublisherRow]]] = {
    concernPublisherDAO.listNewsPublisherByConcerns(uid, (page - 1) * count, count).map(Right(_)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserConcernPublisherService.listConcernPublisher($uid): ${e.getMessage}")
        Left(ExecutionFail(e.getMessage))
    }
  }

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def loadNewsByConcernedPublishers(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Either[DBExceptionMessage, Seq[NewsFeedResponse]]] = {
    concernPublisherDAO.loadNewsByConcernedPublishers(uid, (page - 1) * count, count, msecondsToDatetime(timeCursor)).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.map(Right(_)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserConcernPublisherService.loadNewsByConcernedPublishers($uid, $timeCursor): ${e.getMessage}")
        Left(ExecutionFail(e.getMessage))
    }
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    if (reqTimeCursor.isBefore(oldTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def refreshNewsByConcernedPublishers(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Either[DBExceptionMessage, Seq[NewsFeedResponse]]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
    concernPublisherDAO.refreshNewsByConcernedPublishers(uid, (page - 1) * count, count, newTimeCursor).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.map(Right(_)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserConcernPublisherService.loadNewsByConcernedPublishers($uid, $timeCursor): ${e.getMessage}")
        Left(ExecutionFail(e.getMessage))
    }
  }
}

