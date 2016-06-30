package commons.models.community

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class Baike(title: String, url: String, abs: String)

object Baike {
  implicit val BaikeRowWrites: Writes[Baike] = (
    (JsPath \ "title").write[String] ~
    (JsPath \ "url").write[String] ~
    (JsPath \ "abs").write[String]
  )(unlift(Baike.unapply))

  implicit val BaikeRowReads: Reads[Baike] = (
    (JsPath \ "title").read[String] ~
    (JsPath \ "url").read[String] ~
    (JsPath \ "abs").read[String]
  )(Baike.apply _)
}

case class BaikeRow(id: Option[Long] = None, ctime: LocalDateTime, refer: String, baike: Baike)
