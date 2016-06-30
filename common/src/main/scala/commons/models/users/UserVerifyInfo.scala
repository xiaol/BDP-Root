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
