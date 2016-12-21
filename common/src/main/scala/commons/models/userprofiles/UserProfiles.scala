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
                      imei: Option[String] = None,
                      imeiori: Option[String] = None,
                      mac: Option[String] = None,
                      macori: Option[String] = None,
                      mac1: Option[String] = None,
                      idfa: Option[String] = None,
                      idfaori: Option[String] = None,
                      aaid: Option[String] = None,
                      anid: Option[String] = None,
                      anidori: Option[String] = None,
                      udid: Option[String] = None,
                      brand: Option[String] = None,
                      platform: Option[String] = None,
                      os: Option[String] = None,
                      os_version: Option[String] = None,
                      device_size: Option[String] = None,
                      network: Option[String] = None,
                      operator: Option[String] = None,
                      longitude: Option[String] = None,
                      latitude: Option[String] = None,
                      screen_orientation: Option[String] = None)

object UserDevice {
  implicit val DeviceWrites: Writes[UserDevice] = (
    (JsPath \ "uid").write[String] ~
    (JsPath \ "imei").writeNullable[String] ~
    (JsPath \ "imeiori").writeNullable[String] ~
    (JsPath \ "mac").writeNullable[String] ~
    (JsPath \ "macori").writeNullable[String] ~
    (JsPath \ "mac1").writeNullable[String] ~
    (JsPath \ "idfa").writeNullable[String] ~
    (JsPath \ "idfaori").writeNullable[String] ~
    (JsPath \ "aaid").writeNullable[String] ~
    (JsPath \ "anid").writeNullable[String] ~
    (JsPath \ "anidori").writeNullable[String] ~
    (JsPath \ "udid").writeNullable[String] ~
    (JsPath \ "brand").writeNullable[String] ~
    (JsPath \ "platform").writeNullable[String] ~
    (JsPath \ "os").writeNullable[String] ~
    (JsPath \ "os_version").writeNullable[String] ~
    (JsPath \ "device_size").writeNullable[String] ~
    (JsPath \ "network").writeNullable[String] ~
    (JsPath \ "operator").writeNullable[String] ~
    (JsPath \ "longitude").writeNullable[String] ~
    (JsPath \ "latitude").writeNullable[String] ~
    (JsPath \ "screen_orientation").writeNullable[String]
  )(unlift(UserDevice.unapply))

  implicit val DeviceReads: Reads[UserDevice] = (
    (JsPath \ "uid").read[String] ~
    (JsPath \ "imei").readNullable[String] ~
    (JsPath \ "imeiori").readNullable[String] ~
    (JsPath \ "mac").readNullable[String] ~
    (JsPath \ "macori").readNullable[String] ~
    (JsPath \ "mac1").readNullable[String] ~
    (JsPath \ "idfa").readNullable[String] ~
    (JsPath \ "idfaori").readNullable[String] ~
    (JsPath \ "aaid").readNullable[String] ~
    (JsPath \ "anid").readNullable[String] ~
    (JsPath \ "anidori").readNullable[String] ~
    (JsPath \ "udid").readNullable[String] ~
    (JsPath \ "brand").readNullable[String] ~
    (JsPath \ "platform").readNullable[String] ~
    (JsPath \ "os").readNullable[String] ~
    (JsPath \ "os_version").readNullable[String] ~
    (JsPath \ "device_size").readNullable[String] ~
    (JsPath \ "network").readNullable[String] ~
    (JsPath \ "operator").readNullable[String] ~
    (JsPath \ "longitude").readNullable[String] ~
    (JsPath \ "latitude").readNullable[String] ~
    (JsPath \ "screen_orientation").readNullable[String]
  )(UserDevice.apply _)

  def from(device: Device, uid: Long): UserDevice = {
    UserDevice(uid.toString, device.imei, device.imeiori, device.mac, device.macori, device.mac1, device.idfa, device.idfaori,
      device.aaid, device.anid, device.anidori, device.udid, device.brand, device.platform, device.os, device.os_version,
      device.device_size, device.network, device.operator, device.longitude, device.latitude, device.screen_orientation)
  }
}