package commons.models.spiders

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhange on 2016-05-05.
 *
 */

case class SourceRow(
    id: Option[Long] = None,
    ctime: LocalDateTime,
    surl: Option[String],
    sname: String,
    descr: Option[String] = None,
    queue: String,
    rate: Int,
    status: Int,
    cname: String,
    cid: Long,
    pconf: Option[JsValue] = None,
    state: Int) {

  if (surl.isDefined) require(!surl.get.isEmpty, "surl must be non empty.")
  require(!sname.isEmpty, "sname must be non empty.")
  require(!queue.isEmpty, "queue must be non empty.")
  require(rate > 0, "rate must be greater than 1, in minutes.")
  require(Set(0, 1).contains(status), "status must be 0 or 1.")
  require(!cname.isEmpty, "cname must be non empty.")
  require(cid > 1L, "cid must be greater than 1.")
  require(Set(0, 1).contains(state), "state must be 0 or 1.")
}

object SourceRow {
  implicit val SourceRowWrites: Writes[SourceRow] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "surl").writeNullable[String] ~
    (JsPath \ "sname").write[String] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "queue").write[String] ~
    (JsPath \ "rate").write[Int] ~
    (JsPath \ "status").write[Int] ~
    (JsPath \ "cname").write[String] ~
    (JsPath \ "cid").write[Long] ~
    (JsPath \ "pconf").writeNullable[JsValue] ~
    (JsPath \ "state").write[Int]
  )(unlift(SourceRow.unapply))

  implicit val SourceRowReads: Reads[SourceRow] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "surl").readNullable[String](minLength[String](1)) ~
    (JsPath \ "sname").read[String](minLength[String](1)) ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "queue").read[String](minLength[String](1)) ~
    (JsPath \ "rate").read[Int](min(0)) ~
    (JsPath \ "status").read[Int](min(0) keepAnd max(1)) ~
    (JsPath \ "cname").read[String](minLength[String](1)) ~
    (JsPath \ "cid").read[Long](min(2L)) ~
    (JsPath \ "pconf").readNullable[JsValue] ~
    (JsPath \ "state").read[Int](min(0) keepAnd max(1))
  )(SourceRow.apply _)
}