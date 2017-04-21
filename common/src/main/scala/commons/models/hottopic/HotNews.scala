package commons.models.hottopic

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by fengjigang on 17/4/20.
 */
case class HotNews(nid: Long, ctime: LocalDateTime, status: Int, source: String)

object HotNews {
  implicit val HotNewsWrites: Writes[HotNews] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "status").write[Int] ~
    (JsPath \ "hotword").write[String]
  )(unlift(HotNews.unapply))

  implicit val HotNewsReads: Reads[HotNews] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "status").read[Int] ~
    (JsPath \ "hotword").read[String]
  )(HotNews.apply _)
}
