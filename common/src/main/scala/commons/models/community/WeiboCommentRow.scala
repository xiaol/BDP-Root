package commons.models.community

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class WeiboComment(
  content: String,
  ptime: LocalDateTime,
  commend: Int,
  uname: String,
  uid: String,
  wid: String,
  avatar: Option[String] = None)

object WeiboComment {
  implicit val WeiboCommentRowWrites: Writes[WeiboComment] = (
    (JsPath \ "content").write[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "commend").write[Int] ~
    (JsPath \ "uname").write[String] ~
    (JsPath \ "uid").write[String] ~
    (JsPath \ "wid").write[String] ~
    (JsPath \ "avatar").writeNullable[String]
  )(unlift(WeiboComment.unapply))

  implicit val WeiboCommentRowReads: Reads[WeiboComment] = (
    (JsPath \ "content").read[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "commend").read[Int] ~
    (JsPath \ "uname").read[String] ~
    (JsPath \ "uid").read[String] ~
    (JsPath \ "wid").read[String] ~
    (JsPath \ "avatar").readNullable[String]
  )(WeiboComment.apply _)
}

case class WeiboCommentRow(id: Option[Long] = None, ctime: LocalDateTime, refer: String, comment: WeiboComment)
