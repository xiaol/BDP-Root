package commons.models.userprofiles

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhangshl on 16/11/29.
 */
case class Searchnewslist(id: Option[Int] = None,
                          uid: Long,
                          keywords: String,
                          ctime: Option[LocalDateTime])

object Searchnewslist {
  implicit val SearchnewslistWrites: Writes[Searchnewslist] = (
    (JsPath \ "id").writeNullable[Int] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "keywords").write[String] ~
    (JsPath \ "ctime").writeNullable[LocalDateTime]
  )(unlift(Searchnewslist.unapply))

  implicit val SearchnewslistReads: Reads[Searchnewslist] = (
    (JsPath \ "id").readNullable[Int] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "keywords").read[String] ~
    (JsPath \ "ctime").readNullable[LocalDateTime]
  )(Searchnewslist.apply _)
}