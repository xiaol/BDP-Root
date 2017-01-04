package services.userprofiles

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.{ Device, AdRequest }
import commons.models.userprofiles._
import dao.userprofiles.UserDeviceDAO
import dao.users.{ AppInfoDAO, UserProfilesInfoDAO }
import org.joda.time.LocalDateTime
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangsl on 2016-11-16.
 *
 */

@ImplementedBy(classOf[UserProfileService])
trait IUserProfileService {
  def insert(userProfiles: UserProfiles): Future[Long]
  def delete(uid: Long): Future[Option[Long]]
}

class UserProfileService @Inject() (val userProfilesDAO: UserProfilesInfoDAO, val appInfoDAO: AppInfoDAO, val userDeviceDAO: UserDeviceDAO)
    extends IUserProfileService {

  def insert(userProfiles: UserProfiles): Future[Long] = {
    appInfoDAO.delete(userProfiles.uid)
    userProfiles.apps.map { list => list.map { info => appInfoDAO.insert(info.copy(uid = Some(userProfiles.uid)).copy(ctime = Some(LocalDateTime.now()))) } }

    userProfilesDAO.delete(userProfiles.uid)
    userProfilesDAO.insert(UserProfilesInfo.from(userProfiles).copy(ctime = Some(LocalDateTime.now()))).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserProfileService.insert(${userProfiles.toString}): ${e.getMessage}")
        0L
    }
  }

  def delete(uid: Long): Future[Option[Long]] = {
    userProfilesDAO.delete(uid).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserProfileService.delete($uid): ${e.getMessage}")
        None
    }
  }

  def phone(uid: Long, body: String): Future[Long] = {
    val adRequest: AdRequest = Json.parse(body).as[AdRequest]
    val device: Device = adRequest.device
    userDeviceDAO.findByuid(uid.toString).map {
      _ match {
        case None => userDeviceDAO.insert(UserDevice.from(device, uid)).map { uid => uid.toLong }
        case _    =>
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserProfileService.phone($uid): ${e.getMessage}")
    }
    Future.successful(uid)
  }

}