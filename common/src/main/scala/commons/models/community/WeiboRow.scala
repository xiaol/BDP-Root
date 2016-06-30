package commons.models.community

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class Weibo(
  url: String,
  content: String,
  ptime: LocalDateTime,
  uame: String,
  commend: Int,
  repost: Int,
  comment: Int,
  avatar: Option[String] = None,
  img: Option[String] = None,
  imgs: Option[List[String]] = None)

object Weibo {
  implicit val WeiboRowWrites: Writes[Weibo] = (
    (JsPath \ "url").write[String] ~
    (JsPath \ "content").write[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "uame").write[String] ~
    (JsPath \ "commend").write[Int] ~
    (JsPath \ "repost").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "avatar").writeNullable[String] ~
    (JsPath \ "img").writeNullable[String] ~
    (JsPath \ "imgs").writeNullable[List[String]]
  )(unlift(Weibo.unapply))

  implicit val WeiboRowReads: Reads[Weibo] = (
    (JsPath \ "url").read[String] ~
    (JsPath \ "content").read[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "uame").read[String] ~
    (JsPath \ "commend").read[Int] ~
    (JsPath \ "repost").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "avatar").readNullable[String] ~
    (JsPath \ "img").readNullable[String] ~
    (JsPath \ "imgs").readNullable[List[String]]
  )(Weibo.apply _)
}

case class WeiboRow(id: Option[Long] = None, ctime: LocalDateTime, refer: String, weibo: Weibo)
