package commons.models.advertisement

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{ Reads, JsPath, Writes }

/**
 * Created by zhangshl on 16/9/9.
 */

case class RequestParams(cid: Long,
                         scid: Option[Long] = None,
                         p: Option[Long] = None,
                         c: Option[Long] = None,
                         tcr: Long,
                         tmk: Option[Int] = None,
                         uid: Long,
                         b: String,
                         t: Option[Int] = None)

object RequestParams {
  implicit val RequestParamsWrites: Writes[RequestParams] = (
    (JsPath \ "cid").write[Long] ~
    (JsPath \ "scid").writeNullable[Long] ~
    (JsPath \ "p").writeNullable[Long] ~
    (JsPath \ "c").writeNullable[Long] ~
    (JsPath \ "tcr").write[Long] ~
    (JsPath \ "tmk").writeNullable[Int] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "b").write[String] ~
    (JsPath \ "t").writeNullable[Int]
  )(unlift(RequestParams.unapply))

  implicit val ChannelRowReads: Reads[RequestParams] = (
    (JsPath \ "cid").read[Long] ~
    (JsPath \ "scid").readNullable[Long] ~
    (JsPath \ "p").readNullable[Long] ~
    (JsPath \ "c").readNullable[Long] ~
    (JsPath \ "tcr").read[Long] ~
    (JsPath \ "tmk").readNullable[Int] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "b").read[String] ~
    (JsPath \ "t").readNullable[Int]
  )(RequestParams.apply _)
}