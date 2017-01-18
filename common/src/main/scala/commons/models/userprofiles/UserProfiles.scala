package commons.models.userprofiles

import commons.models.advertisement.Device
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhangshl on 16/11/16.
 */
case class UserProfiles(
  id: Option[Long] = None,
  uid: Long,
  province: Option[String],
  city: Option[String],
  area: Option[String],
  brand: Option[String],
  model: Option[String],
  apps: Option[List[AppInfo]],
  ctime: Option[LocalDateTime])

object UserProfiles {
  implicit val UserProfilesWrites: Writes[UserProfiles] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "area").writeNullable[String] ~
    (JsPath \ "brand").writeNullable[String] ~
    (JsPath \ "model").writeNullable[String] ~
    (JsPath \ "apps").writeNullable[List[AppInfo]] ~
    (JsPath \ "ctime").writeNullable[LocalDateTime]
  )(unlift(UserProfiles.unapply))

  implicit val UserProfilesReads: Reads[UserProfiles] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "area").readNullable[String] ~
    (JsPath \ "brand").readNullable[String] ~
    (JsPath \ "model").readNullable[String] ~
    (JsPath \ "apps").readNullable[List[AppInfo]] ~
    (JsPath \ "ctime").readNullable[LocalDateTime]
  )(UserProfiles.apply _)
}

case class UserProfilesInfo(
  id: Option[Long] = None,
  uid: Long,
  province: Option[String],
  city: Option[String],
  area: Option[String],
  brand: Option[String],
  model: Option[String],
  ctime: Option[LocalDateTime])

object UserProfilesInfo {
  implicit val UserProfilesInfoWrites: Writes[UserProfilesInfo] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "area").writeNullable[String] ~
    (JsPath \ "brand").writeNullable[String] ~
    (JsPath \ "model").writeNullable[String] ~
    (JsPath \ "ctime").writeNullable[LocalDateTime]
  )(unlift(UserProfilesInfo.unapply))

  implicit val UserProfilesInfoReads: Reads[UserProfilesInfo] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "area").readNullable[String] ~
    (JsPath \ "brand").readNullable[String] ~
    (JsPath \ "model").readNullable[String] ~
    (JsPath \ "ctime").readNullable[LocalDateTime]
  )(UserProfilesInfo.apply _)

  def from(userProfiles: UserProfiles): UserProfilesInfo = {
    UserProfilesInfo(None, userProfiles.uid, userProfiles.province, userProfiles.city, userProfiles.area, userProfiles.brand, userProfiles.model, userProfiles.ctime)
  }
}

case class AppInfo(id: Option[Long] = None,
                   uid: Option[Long] = None,
                   app_id: Option[String],
                   app_name: Option[String],
                   active: Option[Int],
                   ctime: Option[LocalDateTime])

object AppInfo {
  implicit val AppInfoWrites: Writes[AppInfo] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "uid").writeNullable[Long] ~
    (JsPath \ "app_id").writeNullable[String] ~
    (JsPath \ "app_name").writeNullable[String] ~
    (JsPath \ "active").writeNullable[Int] ~
    (JsPath \ "ctime").writeNullable[LocalDateTime]
  )(unlift(AppInfo.unapply))

  implicit val AppInfoReads: Reads[AppInfo] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "uid").readNullable[Long] ~
    (JsPath \ "app_id").readNullable[String] ~
    (JsPath \ "app_name").readNullable[String] ~
    (JsPath \ "active").readNullable[Int] ~
    (JsPath \ "ctime").readNullable[LocalDateTime]
  )(AppInfo.apply _)
}

case class UserDevice(uid: String,
                      device: Device,
                      ctype: Option[Int] = None,
                      province: Option[String] = None,
                      city: Option[String] = None,
                      area: Option[String] = None,
                      ptype: Option[Int] = None)

object UserDevice {
  implicit val UserDeviceWrites: Writes[UserDevice] = (
    (JsPath \ "uid").write[String] ~
    (JsPath \ "device").write[Device] ~
    (JsPath \ "ctype").writeNullable[Int] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "area").writeNullable[String] ~
    (JsPath \ "ptype").writeNullable[Int]
  )(unlift(UserDevice.unapply))

  implicit val UserDeviceReads: Reads[UserDevice] = (
    (JsPath \ "uid").read[String] ~
    (JsPath \ "device").read[Device] ~
    (JsPath \ "ctype").readNullable[Int] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "area").readNullable[String] ~
    (JsPath \ "ptype").readNullable[Int]
  )(UserDevice.apply _)
}