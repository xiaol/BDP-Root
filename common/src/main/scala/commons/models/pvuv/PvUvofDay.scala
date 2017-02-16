package commons.models.pvuv

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhangshl on 17/1/9.
 */
case class PvUvofDay(id: Int,
                     pv: Option[Long],
                     data_time_count: LocalDateTime,
                     androidpv: Option[Long],
                     iospv: Option[Long],
                     androiduv: Option[Long],
                     iosuv: Option[Long],
                     adpv: Option[Long],
                     ctype: Option[Int])

object PvUvofDay {
  implicit val PvUvofDayWrites: Writes[PvUvofDay] = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "pv").writeNullable[Long] ~
    (JsPath \ "data_time_count").write[LocalDateTime] ~
    (JsPath \ "androidpv").writeNullable[Long] ~
    (JsPath \ "iospv").writeNullable[Long] ~
    (JsPath \ "androiduv").writeNullable[Long] ~
    (JsPath \ "iosuv").writeNullable[Long] ~
    (JsPath \ "adpv").writeNullable[Long] ~
    (JsPath \ "ctype").writeNullable[Int]
  )(unlift(PvUvofDay.unapply))
}