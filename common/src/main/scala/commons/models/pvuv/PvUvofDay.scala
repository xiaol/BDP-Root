package commons.models.pvuv

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhangshl on 17/1/9.
 */
case class PvUvofDay(id: Int,
                     pv: Long,
                     data_time_count: LocalDateTime,
                     androidpv: Long,
                     iospv: Long,
                     androiduv: Long,
                     iosuv: Long)

object PvUvofDay {
  implicit val PvUvofDayWrites: Writes[PvUvofDay] = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "pv").write[Long] ~
    (JsPath \ "data_time_count").write[LocalDateTime] ~
    (JsPath \ "androidpv").write[Long] ~
    (JsPath \ "iospv").write[Long] ~
    (JsPath \ "androiduv").write[Long] ~
    (JsPath \ "iosuv").write[Long]
  )(unlift(PvUvofDay.unapply))
}