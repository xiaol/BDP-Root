package commons.models.users

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by zhange on 2016-04-25.
 *
 */

case class UserVerifyInfo(email: String, verification: String)

object UserVerifyInfo {
  implicit val UserVerifyInfoWrites: Writes[UserVerifyInfo] = (
    (JsPath \ "email").write[String] ~
    (JsPath \ "verification").write[String]
  )(unlift(UserVerifyInfo.unapply))

  implicit val UserVerifyInfoReads: Reads[UserVerifyInfo] = (
    (JsPath \ "email").read[String] ~
    (JsPath \ "verification").read[String]
  )(UserVerifyInfo.apply _)
}

case class UserResetPasswordInfo(email: String, verification: Option[String])

object UserResetPasswordInfo {
  implicit val UserResetPasswordInfoWrites: Writes[UserResetPasswordInfo] = (
    (JsPath \ "email").write[String] ~
    (JsPath \ "verification").writeNullable[String]
  )(unlift(UserResetPasswordInfo.unapply))

  implicit val UserResetPasswordInfoReads: Reads[UserResetPasswordInfo] = (
    (JsPath \ "email").read[String] ~
    (JsPath \ "verification").readNullable[String]
  )(UserResetPasswordInfo.apply _)
}

case class UserChangePasswordInfo(email: String, oldpassword: String, newpassword: String, verification: Option[String])

object UserChangePasswordInfo {
  implicit val UserResetPasswordInfoWrites: Writes[UserChangePasswordInfo] = (
    (JsPath \ "email").write[String] ~
    (JsPath \ "oldpassword").write[String] ~
    (JsPath \ "newpassword").write[String] ~
    (JsPath \ "verification").writeNullable[String]
  )(unlift(UserChangePasswordInfo.unapply))

  implicit val UserResetPasswordInfoReads: Reads[UserChangePasswordInfo] = (
    (JsPath \ "email").read[String] ~
    (JsPath \ "oldpassword").read[String] ~
    (JsPath \ "newpassword").read[String] ~
    (JsPath \ "verification").readNullable[String]
  )(UserChangePasswordInfo.apply _)
}
