package commons.models.channels

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
 * Created by zhange on 2016-07-26.
 *
 */

case class SeChannelRow(
  id: Option[Long] = None,
  cname: String,
  chid: Long,
  state: Int,
  des: Option[String] = None)

object SeChannelRow {
  implicit val SeChannelRowWrites: Writes[SeChannelRow] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "cname").write[String] ~
    (JsPath \ "chid").write[Long] ~
    (JsPath \ "state").write[Int] ~
    (JsPath \ "des").writeNullable[String]
  )(unlift(SeChannelRow.unapply))

  implicit val SeChannelRowReads: Reads[SeChannelRow] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "cname").read[String](minLength[String](1)) ~
    (JsPath \ "chid").read[Long] ~
    (JsPath \ "state").read[Int](min(0) keepAnd max(1)) ~
    (JsPath \ "des").readNullable[String]
  )(SeChannelRow.apply _)
}