package commons.models.advertisement

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{ JsPath, Reads, Writes }

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
                         t: Option[Int] = None,
                         s: Option[Int] = None,
                         v: Option[Int] = None,
                         nid: Option[Long] = None,
                         ads: Option[Int] = None)

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
    (JsPath \ "t").writeNullable[Int] ~
    (JsPath \ "s").writeNullable[Int] ~
    (JsPath \ "v").writeNullable[Int] ~
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "ads").writeNullable[Int]
  )(unlift(RequestParams.unapply))

  implicit val RequestParamsReads: Reads[RequestParams] = (
    (JsPath \ "cid").read[Long] ~
    (JsPath \ "scid").readNullable[Long] ~
    (JsPath \ "p").readNullable[Long] ~
    (JsPath \ "c").readNullable[Long] ~
    (JsPath \ "tcr").read[Long] ~
    (JsPath \ "tmk").readNullable[Int] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "b").read[String] ~
    (JsPath \ "t").readNullable[Int] ~
    (JsPath \ "s").readNullable[Int] ~
    (JsPath \ "v").readNullable[Int] ~
    (JsPath \ "nid").readNullable[Long] ~
    (JsPath \ "ads").readNullable[Int]
  )(RequestParams.apply _)
}

case class RequestAdvertiseParams(uid: Long,
                                  b: String,
                                  s: Option[Int] = None,
                                  nid: Option[Long] = None)

object RequestAdvertiseParams {
  implicit val RequestAdvertiseParamsWrites: Writes[RequestAdvertiseParams] = (
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "b").write[String] ~
    (JsPath \ "s").writeNullable[Int] ~
    (JsPath \ "nid").writeNullable[Long]
  )(unlift(RequestAdvertiseParams.unapply))

  implicit val RequestAdvertiseParamsReads: Reads[RequestAdvertiseParams] = (
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "b").read[String] ~
    (JsPath \ "s").readNullable[Int] ~
    (JsPath \ "nid").readNullable[Long]
  )(RequestAdvertiseParams.apply _)
}

case class RequestASearchParams(nid: Long, b: Option[String] = None, s: Option[Int] = None, p: Option[Long] = None, c: Option[Long] = None, ads: Option[Int])

object RequestASearchParams {
  implicit val RequestAdvertiseParamsWrites: Writes[RequestASearchParams] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "b").writeNullable[String] ~
    (JsPath \ "s").writeNullable[Int] ~
    (JsPath \ "p").writeNullable[Long] ~
    (JsPath \ "c").writeNullable[Long] ~
    (JsPath \ "ads").writeNullable[Int]
  )(unlift(RequestASearchParams.unapply))

  implicit val RequestAdvertiseParamsReads: Reads[RequestASearchParams] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "b").readNullable[String] ~
    (JsPath \ "s").readNullable[Int] ~
    (JsPath \ "p").readNullable[Long] ~
    (JsPath \ "c").readNullable[Long] ~
    (JsPath \ "ads").readNullable[Int]
  )(RequestASearchParams.apply _)
}

case class RequestAdSourceParams(uid: Long, did: String, ctype: Int, ptype: Int, aversion: String, ctime: Long)

object RequestAdSourceParams {
  implicit val RequestAdSourceParamsWrites: Writes[RequestAdSourceParams] = (
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "did").write[String] ~
    (JsPath \ "ctype").write[Int] ~
    (JsPath \ "ptype").write[Int] ~
    (JsPath \ "aversion").write[String] ~
    (JsPath \ "ctime").write[Long])(unlift(RequestAdSourceParams.unapply))

  implicit val requestAdSourceParamsReads: Reads[RequestAdSourceParams] = (
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "did").read[String] ~
    (JsPath \ "ctype").read[Int] ~
    (JsPath \ "ptype").read[Int] ~
    (JsPath \ "aversion").read[String] ~
    (JsPath \ "ctime").read[Long]
  )(RequestAdSourceParams.apply _)

}