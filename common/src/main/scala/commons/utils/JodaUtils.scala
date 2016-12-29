package commons.utils

import commons.utils.JodaUtils._
import org.joda.time._
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

/**
 * Created by zhange on 2016-04-20.
 *
 */

trait JodaUtils {
  val timeFormat = DateTimeFormat.forPattern("HH:mm:ss")
  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
  val datetimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def getDateNow(): String = LocalDateTime.now.toString(dateFormat)
  def getTimeNow(): String = LocalDateTime.now.toString(timeFormat)
  def getDatetimeNow(plusHour: Int = 0): String = LocalDateTime.now.plusHours(plusHour).toString(datetimeFormat)
  def getMSecondsNow(): String = DateTime.now.getMillis.toString
  def getYearNow(): String = DateTime.now.getYear.toString

  def msecondsToDatetime(milliseconds: Long): LocalDateTime = {
    dateTimeStr2DateTime(new LocalDateTime(milliseconds).toString(datetimeFormat))
  }

  def dateStr2Date(dateStr: String) = LocalDate.parse(dateStr, dateFormat)
  def timeStr2Time(timeStr: String) = LocalTime.parse(timeStr, timeFormat)
  def dateTimeStr2DateTime(datetimeStr: String) = LocalDateTime.parse(datetimeStr, datetimeFormat)
}

object JodaUtils extends JodaUtils

object JodaOderingImplicits {
  implicit def LocalDateTimeOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan(_ isBefore _)
  implicit def LocalDateTimeReverseOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan(_ isAfter _)
}

trait Joda4PlayJsonImplicits {
  implicit val jodaLocalDatetimeWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    def writes(d: LocalDateTime): JsString = JsString(d.toString(datetimeFormat))
  }

  implicit val jodaLocalDatetimeReads: Reads[LocalDateTime] = new Reads[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsString(str) => JsSuccess(dateTimeStr2DateTime(str))
      case _             => JsError("error.expected.JsString")
    }
  }
}

object Joda4PlayJsonImplicits extends Joda4PlayJsonImplicits

