package commons.models.community

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class Douban(title: String, url: String)

object Douban {
  implicit val DoubanRowWrites: Writes[Douban] = (
    (JsPath \ "title").write[String] ~
    (JsPath \ "url").write[String]
  )(unlift(Douban.unapply))

  implicit val DoubanRowReads: Reads[Douban] = (
    (JsPath \ "title").read[String] ~
    (JsPath \ "url").read[String]
  )(Douban.apply _)
}

case class DoubanRow(id: Option[Long] = None, ctime: LocalDateTime, refer: String, douban: Douban)
