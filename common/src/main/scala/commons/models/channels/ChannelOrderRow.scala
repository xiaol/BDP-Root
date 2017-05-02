package commons.models.channels

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
 * Created by zhangshl on 2017/5/2.
 */

case class ChannelOrderRow(
  id: Long,
  cname: String,
  state: Int,
  des: Option[String] = None,
  channel: Int,
  order_num: Int)

object ChannelOrderRow {
  implicit val ChannelOrderRowWrites: Writes[ChannelOrderRow] = (
    (JsPath \ "id").write[Long] ~
    (JsPath \ "cname").write[String] ~
    (JsPath \ "state").write[Int] ~
    (JsPath \ "des").writeNullable[String] ~
    (JsPath \ "channel").write[Int] ~
    (JsPath \ "order_num").write[Int]
  )(unlift(ChannelOrderRow.unapply))

  implicit val ChannelOrderRowReads: Reads[ChannelOrderRow] = (
    (JsPath \ "id").read[Long] ~
    (JsPath \ "cname").read[String] ~
    (JsPath \ "state").read[Int] ~
    (JsPath \ "des").readNullable[String] ~
    (JsPath \ "channel").read[Int] ~
    (JsPath \ "order_num").read[Int]
  )(ChannelOrderRow.apply _)
}