package services.userprofiles

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.{ Slide, Device, AdRequest }
import commons.models.userprofiles._
import dao.pvuvcount.SlideCountDAO
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

class UserProfileService @Inject() (val userProfilesDAO: UserProfilesInfoDAO, val appInfoDAO: AppInfoDAO, val userDeviceDAO: UserDeviceDAO, val slideCountDAO: SlideCountDAO)
    extends IUserProfileService {

  def insert(userProfiles: UserProfiles): Future[Long] = {
    if (userProfiles.uid > 0) {
      appInfoDAO.delete(userProfiles.uid)
      userProfiles.apps.map { list => list.map { info => appInfoDAO.insert(info.copy(uid = Some(userProfiles.uid)).copy(ctime = Some(LocalDateTime.now()))) } }

      userProfilesDAO.delete(userProfiles.uid)
      userProfilesDAO.insert(UserProfilesInfo.from(userProfiles).copy(ctime = Some(LocalDateTime.now()))).recover {
        case NonFatal(e) =>
          Logger.error(s"Within UserProfileService.insert(${userProfiles.toString}): ${e.getMessage}")
          0L
      }
    } else {
      Future.successful(0L)
    }

  }

  def delete(uid: Long): Future[Option[Long]] = {
    userProfilesDAO.delete(uid).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserProfileService.delete($uid): ${e.getMessage}")
        None
    }
  }

  def phone(uid: Long, body: String, ctype: Int, province: Option[String], city: Option[String], area: Option[String], ptype: Int, remoteAddress: Option[String]): Future[Long] = {
    val adRequest: AdRequest = Json.parse(body).as[AdRequest]
    val device: Device = remoteAddress match {
      case Some(ip) => adRequest.device.copy(ip = remoteAddress)
      case _        => adRequest.device
    }
    userDeviceDAO.findByuid(uid.toString).map {
      _ match {
        case None => userDeviceDAO.insert(UserDevice(uid.toString, device, Some(ctype), province, city, area, Some(ptype), Some(LocalDateTime.now()))).map { uid => uid.toLong }
        case _    => userDeviceDAO.update(UserDevice(uid.toString, device, Some(ctype), province, city, area, Some(ptype), Some(LocalDateTime.now()))).map { uid => uid.toLong }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserProfileService.phone($uid): ${e.getMessage}")
    }
    Future.successful(uid)
  }

  def insertSlide(slide: Slide): Future[Long] = {
    slideCountDAO.insert(slide).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserProfileService.insertSlide(${slide.toString}): ${e.getMessage}")
        0L
    }
  }

}