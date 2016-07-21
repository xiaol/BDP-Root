package commons.models.news

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ Reads, _ }
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhange on 2016-07-13.
 *
 */

case class NewsPublisherRow(
  id: Option[Long] = None,
  ctime: LocalDateTime,
  name: String,
  icon: Option[String] = None,
  descr: Option[String] = None,
  concern: Int = 0)

object NewsPublisherRow {
  implicit val NewsPublisherRowWrites: Writes[NewsPublisherRow] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "name").write[String] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "concern").write[Int]
  )(unlift(NewsPublisherRow.unapply))

  implicit val NewsPublisherRowReads: Reads[NewsPublisherRow] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "name").read[String] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "concern").read[Int]
  )(NewsPublisherRow.apply _)
}