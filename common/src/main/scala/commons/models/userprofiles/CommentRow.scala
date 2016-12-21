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
    pid: Option[String] = None,
    global_id: Option[String] = None) {

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
    (JsPath \ "pid").writeNullable[String] ~
    (JsPath \ "global_id").writeNullable[String]
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
    (JsPath \ "pid").readNullable[String](minLength[String](1)) ~
    (JsPath \ "global_id").readNullable[String]
  )(CommentRow.apply _)

  def from(commentOut: CommentOut): CommentRow = {
    CommentRow(commentOut.id, commentOut.content, 0, LocalDateTime.now(), None, commentOut.uname.getOrElse("匿名用户"), commentOut.avatar, commentOut.docid, None, None, Some(commentOut.uid))
  }
}

case class CommentOut(
    id: Option[Long] = None,
    content: String,
    commend: Option[Int] = None,
    ctime: Option[LocalDateTime] = None,
    uid: String,
    uname: Option[String] = None,
    avatar: Option[String],
    docid: String) {
  require(!content.isEmpty, "content must be non empty.")
  require(!uid.isEmpty, "uid must be non empty.")
  require(!docid.isEmpty, "docid must be non empty.")
}

object CommentOut {
  implicit val CommentOutWrites: Writes[CommentOut] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "content").write[String] ~
    (JsPath \ "commend").writeNullable[Int] ~
    (JsPath \ "ctime").writeNullable[LocalDateTime] ~
    (JsPath \ "uid").write[String] ~
    (JsPath \ "uname").writeNullable[String] ~
    (JsPath \ "avatar").writeNullable[String] ~
    (JsPath \ "docid").write[String]
  )(unlift(CommentOut.unapply))

  implicit val CommentOutReads: Reads[CommentOut] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "content").read[String] ~
    (JsPath \ "commend").readNullable[Int] ~
    (JsPath \ "ctime").readNullable[LocalDateTime] ~
    (JsPath \ "uid").read[String] ~
    (JsPath \ "uname").readNullable[String] ~
    (JsPath \ "avatar").readNullable[String] ~
    (JsPath \ "docid").read[String]
  )(CommentOut.apply _)
}