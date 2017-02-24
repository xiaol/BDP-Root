package commons.models.report

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import commons.utils.Joda4PlayJsonImplicits._
/**
 * Created by zhangshl on 17/2/23.
 */
case class TopClickNews(nid: Long,
                        title: String,
                        clickcount: Option[Int],
                        showcount: Option[Int],
                        ctype: Option[Int],
                        ptype: Option[Int],
                        data_time_count: LocalDateTime)

object TopClickNews {
  implicit val TopClickNewsWrites: Writes[TopClickNews] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "clickcount").writeNullable[Int] ~
    (JsPath \ "showcount").writeNullable[Int] ~
    (JsPath \ "ctype").writeNullable[Int] ~
    (JsPath \ "ptype").writeNullable[Int] ~
    (JsPath \ "data_time_count").write[LocalDateTime]
  )(unlift(TopClickNews.unapply))
}