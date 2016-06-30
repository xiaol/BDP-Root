package commons.models.spiders

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Writes }

/**
 * Created by zhange on 2016-05-17.
 *
 */

case class SourceResponse(
  id: Option[Long] = None,
  sname: String,
  descr: Option[String] = None,
  status: Int,
  cname: String,
  cid: Long,
  state: Int)

object SourceResponse {

  implicit val SourceResponseWrites: Writes[SourceResponse] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "sname").write[String] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "status").write[Int] ~
    (JsPath \ "cname").write[String] ~
    (JsPath \ "cid").write[Long] ~
    (JsPath \ "state").write[Int]
  )(unlift(SourceResponse.unapply))

  def from(source: SourceRow): SourceResponse = {
    SourceResponse(source.id, source.sname, source.descr, source.status, source.cname, source.cid, source.state)
  }
}
