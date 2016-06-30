package commons.models.userprofiles

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class CommentRow(
    id: Option[Long] = None,
    content: String,
    commend: Int,
    ctime: LocalDateTime,
    uid: Option[Long],
    uname: String,
    avatar: Option[String],
    docid: String,
    cid: Option[String] = None,
    pid: Option[String] = None) {

  require(!content.isEmpty, "content must be non empty.")
  if (uid.isDefined) require(uid.get > 0L, "uid must be a positive Long number.")
  require(!uname.isEmpty, "uname must be non empty.")
  if (avatar.isDefined) require(!avatar.get.isEmpty, "avatar must be non empty.")
  require(!docid.isEmpty, "docid must be non empty.")
  if (cid.isDefined) require(!cid.get.isEmpty, "cid must be non empty.")
  if (pid.isDefined) require(!pid.get.isEmpty, "pid must be non empty.")
}

object CommentRow {
  implicit val CommentRowWrites: Writes[CommentRow] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "content").write[String] ~
    (JsPath \ "commend").write[Int] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "uid").writeNullable[Long] ~
    (JsPath \ "uname").write[String] ~
    (JsPath \ "avatar").writeNullable[String] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "cid").writeNullable[String] ~
    (JsPath \ "pid").writeNullable[String]
  )(unlift(CommentRow.unapply))

  implicit val CommentRowReads: Reads[CommentRow] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "content").read[String](minLength[String](1)) ~
    (JsPath \ "commend").read[Int] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "uid").readNullable[Long](min(1L)) ~
    (JsPath \ "uname").read[String](minLength[String](1)) ~
    (JsPath \ "avatar").readNullable[String](minLength[String](1)) ~
    (JsPath \ "docid").read[String](minLength[String](1)) ~
    (JsPath \ "cid").readNullable[String](minLength[String](1)) ~
    (JsPath \ "pid").readNullable[String](minLength[String](1))
  )(CommentRow.apply _)
}
