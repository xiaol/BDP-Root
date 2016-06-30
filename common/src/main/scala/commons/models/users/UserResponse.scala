package commons.models.users

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by zhange on 2016-04-21.
 *
 */

case class UserResponse(
  utype: Int,
  uid: Long,
  password: Option[String] = None,
  uname: Option[String] = None,
  avatar: Option[String] = None,
  channel: Option[List[String]] = None)

object UserResponse {
  implicit val UserResponseWrites: Writes[UserResponse] = (
    (JsPath \ "utype").write[Int] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "password").writeNullable[String] ~
    (JsPath \ "uname").writeNullable[String] ~
    (JsPath \ "avatar").writeNullable[String] ~
    (JsPath \ "channel").writeNullable[List[String]]
  )(unlift(UserResponse.unapply))
}

