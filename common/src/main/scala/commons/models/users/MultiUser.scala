package commons.models.users

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by zhange on 2016-04-26.
 *
 */

trait User

case class UserGuest(
  utype: Int,
  platform: Int,
  province: Option[String] = None,
  city: Option[String] = None,
  district: Option[String] = None) extends User

object UserGuest {
  implicit val UserGuestReads: Reads[UserGuest] = (
    (JsPath \ "utype").read[Int] ~
    (JsPath \ "platform").read[Int] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String]
  )(UserGuest.apply _)
}

case class UserSocial(
  muid: Option[Long] = None,
  msuid: Option[String] = None,
  utype: Int,
  platform: Int,
  suid: String,
  stoken: String,
  sexpires: LocalDateTime,
  uname: Option[String],
  gender: Option[Int],
  avatar: Option[String] = None,
  averse: Option[List[String]] = None,
  prefer: Option[List[String]] = None,
  province: Option[String] = None,
  city: Option[String] = None,
  district: Option[String] = None) extends User

object UserSocial {
  implicit val UserSocialReads: Reads[UserSocial] = (
    (JsPath \ "muid").readNullable[Long] ~
    (JsPath \ "msuid").readNullable[String] ~
    (JsPath \ "utype").read[Int] ~
    (JsPath \ "platform").read[Int] ~
    (JsPath \ "suid").read[String] ~
    (JsPath \ "stoken").read[String] ~
    (JsPath \ "sexpires").read[LocalDateTime] ~
    (JsPath \ "uname").readNullable[String] ~
    (JsPath \ "gender").readNullable[Int] ~
    (JsPath \ "avatar").readNullable[String] ~
    (JsPath \ "averse").readNullable[List[String]] ~
    (JsPath \ "prefer").readNullable[List[String]] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String]
  )(UserSocial.apply _)
}

case class UserLocal(
  uid: Option[Long] = None,
  utype: Int,
  platform: Int,
  uname: Option[String] = None,
  gender: Option[Int] = None,
  avatar: Option[String] = None,
  email: String,
  password: String,
  averse: Option[List[String]] = None,
  prefer: Option[List[String]] = None,
  province: Option[String] = None,
  city: Option[String] = None,
  district: Option[String] = None) extends User

object UserLocal {
  implicit val UserLocalReads: Reads[UserLocal] = (
    (JsPath \ "uid").readNullable[Long] ~
    (JsPath \ "utype").read[Int] ~
    (JsPath \ "platform").read[Int] ~
    (JsPath \ "uname").readNullable[String] ~
    (JsPath \ "gender").readNullable[Int] ~
    (JsPath \ "avatar").readNullable[String] ~
    (JsPath \ "email").read[String] ~
    (JsPath \ "password").read[String] ~
    (JsPath \ "averse").readNullable[List[String]] ~
    (JsPath \ "prefer").readNullable[List[String]] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String]
  )(UserLocal.apply _)
}