package commons.models.spiders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-11.
 *
 */

case class QueueRow(
  queue: String,
  spider: String,
  descr: Option[String] = None)

object QueueRow {
  implicit val QueueRowWrites: Writes[QueueRow] = (
    (JsPath \ "queue").write[String] ~
    (JsPath \ "spider").write[String] ~
    (JsPath \ "descr").writeNullable[String]
  )(unlift(QueueRow.unapply))

  implicit val QueueRowReads: Reads[QueueRow] = (
    (JsPath \ "queue").read[String](minLength[String](1)) ~
    (JsPath \ "spider").read[String](minLength[String](1)) ~
    (JsPath \ "descr").readNullable[String]
  )(QueueRow.apply _)
}