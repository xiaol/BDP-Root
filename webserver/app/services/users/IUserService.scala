package services.users

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.users._
import commons.utils.PasswordUtils._
import dao.users.UserDAO
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-04-19.
 *
 */

@ImplementedBy(classOf[UserService])
trait IUserService {
  def findByUid(uid: Long): Future[Option[UserRow]]
  def findByEmail(email: String): Future[Option[UserRow]]
  def findByUname(uname: String): Future[Option[UserRow]]
  def findBySuid(suid: String): Future[Option[UserRow]]
  def list(page: Long, count: Long): Future[Seq[UserRow]]
  def count(): Future[Option[Int]]
  def insert(userRow: UserRow): Future[Option[UserRow]]
  def update(uid: Long, userRow: UserRow): Future[Option[UserRow]]
  def updateLoginTime(uid: Long, llogin: LocalDateTime): Future[Option[Long]]
  def updateChannel(uid: Long, channel: List[String]): Future[Option[List[String]]]
  def delete(uid: Long): Future[Option[Long]]
}

class UserService @Inject() (val userListDAO: UserDAO) extends IUserService with UserCacheService {

  def findByUid(uid: Long): Future[Option[UserRow]] = {
    userListDAO.findByUid(uid).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.findByUid($uid): ${e.getMessage}")
        None
    }
  }

  def findByEmail(email: String): Future[Option[UserRow]] = {
    userListDAO.findByEmail(email).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.findByEmail($email): ${e.getMessage}")
        None
    }
  }

  def findByUname(uname: String): Future[Option[UserRow]] = {
    userListDAO.findByUname(uname).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.findByUname($uname): ${e.getMessage}")
        None
    }
  }

  def findBySuid(suid: String): Future[Option[UserRow]] = {
    userListDAO.findBySuid(suid).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.findBySuid($suid): ${e.getMessage}")
        None
    }
  }

  def list(page: Long = 1L, count: Long = 20L): Future[Seq[UserRow]] = {
    userListDAO.list((page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.list($page, $count): ${e.getMessage}")
        Seq[UserRow]()
    }
  }

  def count(): Future[Option[Int]] = {
    userListDAO.count().map { c => Some(c) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.count(): ${e.getMessage}")
        None
    }
  }

  def insert(userRow: UserRow): Future[Option[UserRow]] = {
    userListDAO.insert(userRow).map(uid => Some(userRow.copy(sys = userRow.sys.copy(uid = Some(uid))))).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.insert(${UserRow.toString}): ${e.getMessage}")
        None
    }
  }

  def delete(uid: Long): Future[Option[Long]] = {
    userListDAO.delete(uid).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.delete($uid): ${e.getMessage}")
        None
    }
  }

  def update(uid: Long, userRow: UserRow): Future[Option[UserRow]] = {
    userListDAO.update(uid, userRow).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.update($uid, ${userRow.toString}): ${e.getMessage}")
        None
    }
  }

  def updateLoginTime(uid: Long, llogin: LocalDateTime): Future[Option[Long]] = {
    userListDAO.updateLoginTime(uid, llogin).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.updateLoginTime($uid, ${llogin.toString}): ${e.getMessage}")
        None
    }
  }

  def updateChannel(uid: Long, channel: List[String]): Future[Option[List[String]]] = {
    userListDAO.updateChannel(uid, channel).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.updateChannel($uid, ${channel.toString}): ${e.getMessage}")
        None
    }
  }

  def updateProfile(uid: Long, profile: UserRowProfile): Future[Option[UserRow]] = {
    findByUid(uid).flatMap {
      case Some(userRow) =>
        update(uid, UserRowHelpers.merge(userRow, profile)).map {
          case userOpt @ Some(_) =>
            setUserRowCache(userOpt.get); userOpt
          case _ => None
        }
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.updateProfile($uid, ${profile.toString}): ${e.getMessage}")
        None
    }
  }

  def updateUserRowByUid(uid: Long, updateRow: UserRow): Future[Option[UserRow]] = {
    findByUid(uid).flatMap {
      case Some(userRow) =>
        update(uid, UserRowHelpers.merge(userRow, updateRow)).map {
          case userOpt @ Some(_) =>
            setUserRowCache(userOpt.get); userOpt
          case _ => None
        }
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.updateUserRowByUid($uid, ${updateRow.toString}): ${e.getMessage}")
        None
    }
  }

  def updateUserRowBySuid(suid: String, updateRow: UserRow): Future[Option[UserRow]] = {
    findBySuid(suid).flatMap {
      case Some(userRow) =>
        update(userRow.sys.uid.get, UserRowHelpers.merge(userRow, updateRow)).map {
          case userOpt @ Some(_) =>
            setUserRowCache(userOpt.get); userOpt
          case _ => None
        }
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserService.updateUserRowBySuid($suid, ${updateRow.toString}): ${e.getMessage}")
        None
    }
  }

  def authenticate(userLoginInfo: UserLoginInfo): Future[Option[UserRow]] = {
    userLoginInfo match {
      case UserLoginInfo(Some(id), _, password) =>
        findByUid(id).map {
          case Some(user) if user.base.password.isDefined && password == user.base.password.get => Some(user)
          case _                                                                                => None
        }
      case UserLoginInfo(_, Some(email), password) =>
        findByEmail(email).map {
          case Some(user) if user.base.password.isDefined && user.base.passsalt.isDefined &&
            hashAndStretch(password, user.base.passsalt.get, STRETCH_LOOP_COUNT) == user.base.password.get => Some(user)
          case _ => None
        }
    }
  }

  def updateRole(uid: Long, urole: Int): Future[Option[Long]] = ???

  def updateUname(uid: Long, oldUname: String, newUname: String): Future[Option[Long]] = ???

  def updateEmail(uid: Long, email: String): Future[Option[Long]] = ???

  def updateverified(uid: Long, verify: Boolean): Future[Option[Long]] = ???

  def updatePassword(uid: Long, oldPass: String, newPass: String): Future[Option[Long]] = ???
}
