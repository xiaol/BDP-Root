package commons.models.userprofiles

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Writes }

/**
 * Created by zhange on 2016-05-24.
 *
 */

case class CommentResponse(
    id: Option[Long] = None,
    content: String,
    commend: Int,
    ctime: LocalDateTime,
    uid: Option[Long],
    uname: String,
    avatar: Option[String],
    docid: String,
    upflag: Int = 0,
    nid: Option[Long],
    ntitle: Option[String],
    rtype: Option[Int] = None) {
  require(Set(0, 1).contains(upflag), "upflag must be 0 or 1, 0 means not yet commend.")
}

object CommentResponse {

  implicit val CommentResponseWrites: Writes[CommentResponse] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "content").write[String] ~
    (JsPath \ "commend").write[Int] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "uid").writeNullable[Long] ~
    (JsPath \ "uname").write[String] ~
    (JsPath \ "avatar").writeNullable[String] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "upflag").write[Int] ~
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "ntitle").writeNullable[String] ~
    (JsPath \ "rtype").writeNullable[Int]
  )(unlift(CommentResponse.unapply))

  def from(row: CommentRow, upflag: Int = 0, nid: Option[Long] = None, ntitle: Option[String] = None, rtype: Option[Int] = None): CommentResponse = {
    CommentResponse(row.id, row.content, row.commend, row.ctime, row.uid, row.uname, row.avatar, row.docid, upflag, nid, ntitle, rtype)
  }
}