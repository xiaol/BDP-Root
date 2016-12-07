package services.users

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.users._
import dao.news.NewsrecommendclickDAO
import dao.users.UserReferenceDAO
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangsl on 2016-12-01.
 *
 */

@ImplementedBy(classOf[UserReferenceService])
trait IUserReferenceService {
  def findByUid(uid: String, sys_source: String): Future[Option[UserReferenceRow]]
  def findByGlobal_id(global_id: String): Future[Option[UserReferenceRow]]
  def insert(uid: String, uname: Option[String], sys_source: String): Future[Option[String]]
  def update(global_id: String, uid: String, uname: Option[String], sys_source: String): Future[Option[UserReferenceRow]]
  def delete(global_id: String): Future[Option[String]]
}

class UserReferenceService @Inject() (val userReferenceDAO: UserReferenceDAO, val newsrecommendclickDAO: NewsrecommendclickDAO) extends IUserReferenceService {

  def findByUid(uid: String, sys_source: String): Future[Option[UserReferenceRow]] = {
    userReferenceDAO.findByUid(uid, sys_source).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserReferenceService.findByUid($uid): ${e.getMessage}")
        None
    }
  }

  def findByGlobal_id(global_id: String): Future[Option[UserReferenceRow]] = {
    userReferenceDAO.findByGlobal_id(global_id).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserReferenceService.findByGlobal_id($global_id): ${e.getMessage}")
        None
    }
  }

  def insert(uid: String, uname: Option[String], sys_source: String): Future[Option[String]] = {
    val result: Future[String] = newsrecommendclickDAO.getNextId().flatMap {
      case seq: Seq[Long] if seq.nonEmpty => userReferenceDAO.insert(UserReferenceRow(seq.head, uid, uname, sys_source, (sys_source + "_" + uid + "_" + seq.head), LocalDateTime.now()))
    }
    result.map { id => Some(id) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserReferenceService.insert(${UserRow.toString}): ${e.getMessage}")
        None
    }
  }

  def update(global_id: String, uid: String, uname: Option[String], sys_source: String): Future[Option[UserReferenceRow]] = {
    userReferenceDAO.findByGlobal_id(global_id).flatMap {
      case Some(userReferenceRow: UserReferenceRow) => userReferenceDAO.update(global_id, userReferenceRow.copy(uid = uid).copy(uname = uname).copy(sys_source = sys_source))
      case None                                     => Future.successful(None)
    } recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserReferenceService.update($global_id, $uid, $uname, $sys_source): ${e.getMessage}")
        None
    }
  }

  def delete(global_id: String): Future[Option[String]] = {
    userReferenceDAO.delete(global_id).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserReferenceService.delete($global_id): ${e.getMessage}")
        None
    }
  }

}
