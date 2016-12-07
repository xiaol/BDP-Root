package commons.models.users

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhangshl on 16/12/1.
 */
case class UserReferenceRow(id: Long,
                            uid: String,
                            uname: Option[String],
                            sys_source: String,
                            global_id: String,
                            ctime: LocalDateTime)

object UserReferenceRow {
  implicit val UserReferenceRowWrites: Writes[UserReferenceRow] = (
    (JsPath \ "id").write[Long] ~
    (JsPath \ "uid").write[String] ~
    (JsPath \ "uname").writeNullable[String] ~
    (JsPath \ "sys_source").write[String] ~
    (JsPath \ "global_id").write[String] ~
    (JsPath \ "ctime").write[LocalDateTime]
  )(unlift(UserReferenceRow.unapply))

  implicit val UserReferenceRowReads: Reads[UserReferenceRow] = (
    (JsPath \ "id").read[Long] ~
    (JsPath \ "uid").read[String] ~
    (JsPath \ "uname").readNullable[String] ~
    (JsPath \ "sys_source").read[String] ~
    (JsPath \ "global_id").read[String] ~
    (JsPath \ "ctime").read[LocalDateTime]
  )(UserReferenceRow.apply _)
}