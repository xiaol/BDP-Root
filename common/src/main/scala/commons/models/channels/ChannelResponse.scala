package commons.models.channels

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-07-26.
 *
 */

case class ChannelResponse(id: Option[Long],
                           cname: String,
                           state: Int,
                           des: Option[String] = None,
                           schs: Option[Seq[SeChannelRow]])

object ChannelResponse {
  implicit val ChannelResponseWrites: Writes[ChannelResponse] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "cname").write[String] ~
    (JsPath \ "state").write[Int] ~
    (JsPath \ "des").writeNullable[String] ~
    (JsPath \ "schs").writeNullable[Seq[SeChannelRow]]
  )(unlift(ChannelResponse.unapply))

  def from(channel: ChannelRow, sechs: Option[Seq[SeChannelRow]] = None): ChannelResponse =
    ChannelResponse(channel.id, channel.cname, channel.state, channel.des, sechs)
}
