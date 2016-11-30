package commons.models.userprofiles

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhangshl on 16/11/29.
 */
case class Relaylist(id: Option[Int] = None,
                     nid: Long,
                     uid: Long,
                     whereabout: Int,
                     ctime: Option[LocalDateTime])

object Relaylist {
  implicit val RelaylistWrites: Writes[Relaylist] = (
    (JsPath \ "id").writeNullable[Int] ~
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "whereabout").write[Int] ~
    (JsPath \ "ctime").writeNullable[LocalDateTime]
  )(unlift(Relaylist.unapply))

  implicit val RelaylistReads: Reads[Relaylist] = (
    (JsPath \ "id").readNullable[Int] ~
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "whereabout").read[Int] ~
    (JsPath \ "ctime").readNullable[LocalDateTime]
  )(Relaylist.apply _)
}