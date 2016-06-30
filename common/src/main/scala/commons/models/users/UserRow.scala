package commons.models.users

import commons.utils.Joda4PlayJsonImplicits._
import play.api.libs.json._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._

/**
 * Created by zhange on 2016-04-18.
 *
 */

case class UserRowSys(
  uid: Option[Long] = None,
  ctime: LocalDateTime,
  ltime: LocalDateTime,
  platf: Int,
  urole: Int,
  utype: Int)

object UserRowSys {
  implicit val UserRowSysWrites: Writes[UserRowSys] = (
    (JsPath \ "uid").writeNullable[Long] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "ltime").write[LocalDateTime] ~
    (JsPath \ "platf").write[Int] ~
    (JsPath \ "urole").write[Int] ~
    (JsPath \ "utype").write[Int]
  )(unlift(UserRowSys.unapply))

  implicit val UserRowSysReads: Reads[UserRowSys] = (
    (JsPath \ "uid").readNullable[Long] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "ltime").read[LocalDateTime] ~
    (JsPath \ "platf").read[Int] ~
    (JsPath \ "urole").read[Int] ~
    (JsPath \ "utype").read[Int]
  )(UserRowSys.apply _)
}

case class UserRowBase(
  email: Option[String] = None,
  verified: Option[Boolean] = None,
  password: Option[String] = None,
  passsalt: Option[String] = None,
  uname: Option[String] = None,
  gender: Option[Int] = None,
  avatar: Option[String] = None)

object UserRowBase {
  implicit val UserRowBaseWrites: Writes[UserRowBase] = (
    (JsPath \ "email").writeNullable[String] ~
    (JsPath \ "verified").writeNullable[Boolean] ~
    (JsPath \ "password").writeNullable[String] ~
    (JsPath \ "passsalt").writeNullable[String] ~
    (JsPath \ "uname").writeNullable[String] ~
    (JsPath \ "gender").writeNullable[Int] ~
    (JsPath \ "avatar").writeNullable[String]
  )(unlift(UserRowBase.unapply))

  implicit val UserRowBaseReads: Reads[UserRowBase] = (
    (JsPath \ "email").readNullable[String] ~
    (JsPath \ "verified").readNullable[Boolean] ~
    (JsPath \ "password").readNullable[String] ~
    (JsPath \ "passsalt").readNullable[String] ~
    (JsPath \ "uname").readNullable[String] ~
    (JsPath \ "gender").readNullable[Int] ~
    (JsPath \ "avatar").readNullable[String]
  )(UserRowBase.apply _)
}

case class UserRowProfile(
  channel: Option[List[String]] = None,
  averse: Option[List[String]] = None,
  prefer: Option[List[String]] = None,
  province: Option[String] = None,
  city: Option[String] = None,
  district: Option[String] = None)

object UserRowProfile {
  implicit val UserRowProfileWrites: Writes[UserRowProfile] = (
    (JsPath \ "channel").writeNullable[List[String]] ~
    (JsPath \ "averse").writeNullable[List[String]] ~
    (JsPath \ "prefer").writeNullable[List[String]] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "district").writeNullable[String]
  )(unlift(UserRowProfile.unapply))

  implicit val UserRowProfileReads: Reads[UserRowProfile] = (
    (JsPath \ "channel").readNullable[List[String]] ~
    (JsPath \ "averse").readNullable[List[String]] ~
    (JsPath \ "prefer").readNullable[List[String]] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String]
  )(UserRowProfile.apply _)
}

case class UserRowSocial(
  suid: Option[String] = None,
  stoken: Option[String] = None,
  sexpires: Option[LocalDateTime] = None)

object UserRowSocial {
  implicit val UserRowSocialWrites: Writes[UserRowSocial] = (
    (JsPath \ "suid").writeNullable[String] ~
    (JsPath \ "stoken").writeNullable[String] ~
    (JsPath \ "sexpires").writeNullable[LocalDateTime]
  )(unlift(UserRowSocial.unapply))

  implicit val UserRowSocialReads: Reads[UserRowSocial] = (
    (JsPath \ "suid").readNullable[String] ~
    (JsPath \ "stoken").readNullable[String] ~
    (JsPath \ "sexpires").readNullable[LocalDateTime]
  )(UserRowSocial.apply _)
}

case class UserRow(
  sys: UserRowSys,
  base: UserRowBase,
  profile: UserRowProfile,
  social: UserRowSocial)

object UserRow {
  import UserRowSys._
  import UserRowBase._
  import UserRowProfile._
  import UserRowSocial._

  implicit val UserRowWrites: Writes[UserRow] = (
    (JsPath \ "sys").write[UserRowSys] ~
    (JsPath \ "base").write[UserRowBase] ~
    (JsPath \ "profile").write[UserRowProfile] ~
    (JsPath \ "social").write[UserRowSocial]
  )(unlift(UserRow.unapply))

  implicit val UserRowReads: Reads[UserRow] = (
    (JsPath \ "sys").read[UserRowSys] ~
    (JsPath \ "base").read[UserRowBase] ~
    (JsPath \ "profile").read[UserRowProfile] ~
    (JsPath \ "social").read[UserRowSocial]
  )(UserRow.apply _)
}
