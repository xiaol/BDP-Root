package commons.models.userprofiles

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhangshl on 16/11/29.
 */
case class Hatenewslist(id: Option[Int] = None,
                        nid: Long,
                        uid: Long,
                        reason: Option[Int] = None,
                        ctime: Option[LocalDateTime])

object Hatenewslist {
  implicit val HatenewslistWrites: Writes[Hatenewslist] = (
    (JsPath \ "id").writeNullable[Int] ~
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "reason").writeNullable[Int] ~
    (JsPath \ "ctime").writeNullable[LocalDateTime]
  )(unlift(Hatenewslist.unapply))

  implicit val HatenewslistReads: Reads[Hatenewslist] = (
    (JsPath \ "id").readNullable[Int] ~
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "reason").readNullable[Int] ~
    (JsPath \ "ctime").readNullable[LocalDateTime]
  )(Hatenewslist.apply _)
}