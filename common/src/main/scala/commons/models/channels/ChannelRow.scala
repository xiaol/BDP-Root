package commons.models.channels

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class ChannelRow(
    id: Option[Long] = None,
    cname: String,
    state: Int,
    des: Option[String] = None) {

  require(!cname.isEmpty, "cname must be non empty.")
  require(Set(0, 1).contains(state), "cname must be 0 or 1.")
}

object ChannelRow {
  implicit val ChannelRowWrites: Writes[ChannelRow] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "cname").write[String] ~
    (JsPath \ "state").write[Int] ~
    (JsPath \ "des").writeNullable[String]
  )(unlift(ChannelRow.unapply))

  implicit val ChannelRowReads: Reads[ChannelRow] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "cname").read[String](minLength[String](1)) ~
    (JsPath \ "state").read[Int](min(0) keepAnd max(1)) ~
    (JsPath \ "des").readNullable[String]
  )(ChannelRow.apply _)
}