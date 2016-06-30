package commons.models.users

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by zhange on 2016-04-24.
 *
 */

case class UserLoginInfo(uid: Option[Long] = None, email: Option[String] = None, password: String)

object UserLoginInfo {
  implicit val UserLoginInfoReads: Reads[UserLoginInfo] = (
    (JsPath \ "uid").readNullable[Long] ~
    (JsPath \ "email").readNullable[String] ~
    (JsPath \ "password").read[String]
  )(UserLoginInfo.apply _)
}
