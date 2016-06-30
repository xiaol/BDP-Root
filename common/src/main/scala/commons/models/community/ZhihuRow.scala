package commons.models.community

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class Zhihu(url: String, title: String, author: String)

object Zhihu {
  implicit val ZhihuRowWrites: Writes[Zhihu] = (
    (JsPath \ "url").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "author").write[String]
  )(unlift(Zhihu.unapply))

  implicit val ZhihuRowReads: Reads[Zhihu] = (
    (JsPath \ "url").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "author").read[String]
  )(Zhihu.apply _)
}

case class ZhihuRow(id: Option[Long] = None, ctime: LocalDateTime, refer: String, zhihu: Zhihu)
