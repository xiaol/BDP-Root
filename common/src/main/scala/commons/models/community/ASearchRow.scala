package commons.models.community

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class ASearchTemp(
  url: String,
  title: String,
  searchFrom: String,
  rank: Int,
  sourceSite: String,
  updateTime: LocalDateTime,
  imgUrl: Option[String] = None,
  abs: Option[String] = None)

object ASearchTemp {
  implicit val ASearchTempWrites: Writes[ASearchTemp] = (
    (JsPath \ "url").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "searchFrom").write[String] ~
    (JsPath \ "rank").write[Int] ~
    (JsPath \ "sourceSite").write[String] ~
    (JsPath \ "updateTime").write[LocalDateTime] ~
    (JsPath \ "imgUrl").writeNullable[String] ~
    (JsPath \ "abs").writeNullable[String]
  )(unlift(ASearchTemp.unapply))

  implicit val ASearchTempReads: Reads[ASearchTemp] = (
    (JsPath \ "url").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "searchFrom").read[String] ~
    (JsPath \ "rank").read[Int] ~
    (JsPath \ "sourceSite").read[String] ~
    (JsPath \ "updateTime").read[LocalDateTime] ~
    (JsPath \ "imgUrl").readNullable[String] ~
    (JsPath \ "abs").readNullable[String]
  )(ASearchTemp.apply _)
}

case class ASearchTemps(asearchs: List[ASearchTemp])

object ASearchTemps {
  implicit val SearchItemsTempFormat: Format[ASearchTemps] =
    (__ \ "searchItems").format[List[ASearchTemp]].inmap(items => ASearchTemps(items), (asearchTemps: ASearchTemps) => asearchTemps.asearchs)
}

case class ASearch(
  url: String,
  title: String,
  from: String,
  rank: Int,
  pname: String,
  ptime: LocalDateTime,
  img: Option[String] = None,
  abs: Option[String] = None,
  nid: Option[Long] = None,
  duration: Option[Int] = None,
  logtype: Option[Int] = Some(26),
  logchid: Option[Int] = Some(0))

object ASearch {
  implicit val ASearchWrites: Writes[ASearch] = (
    (JsPath \ "url").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "from").write[String] ~
    (JsPath \ "rank").write[Int] ~
    (JsPath \ "pname").write[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "img").writeNullable[String] ~
    (JsPath \ "abs").writeNullable[String] ~
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "duration").writeNullable[Int] ~
    (JsPath \ "logtype").writeNullable[Int] ~
    (JsPath \ "logchid").writeNullable[Int]
  )(unlift(ASearch.unapply))

  implicit val ASearchReads: Reads[ASearch] = (
    (JsPath \ "url").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "from").read[String] ~
    (JsPath \ "rank").read[Int] ~
    (JsPath \ "pname").read[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "img").readNullable[String] ~
    (JsPath \ "abs").readNullable[String] ~
    (JsPath \ "nid").readNullable[Long] ~
    (JsPath \ "duration").readNullable[Int] ~
    (JsPath \ "logtype").readNullable[Int] ~
    (JsPath \ "logchid").readNullable[Int]
  )(ASearch.apply _)

  def from(temp: ASearchTemp): ASearch = {
    ASearch(temp.url, temp.title, temp.searchFrom, temp.rank, temp.sourceSite, temp.updateTime, temp.imgUrl, temp.abs)
  }
}

case class ASearchRow(id: Option[Long] = None, ctime: LocalDateTime, refer: String, asearch: ASearch)

object ASearchRow {
  implicit val ASearchRowWrites: Writes[ASearchRow] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "refer").write[String] ~
    (JsPath \ "asearch").write[ASearch]
  )(unlift(ASearchRow.unapply))

  implicit val ASearchRowReads: Reads[ASearchRow] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "refer").read[String] ~
    (JsPath \ "asearch").read[ASearch]
  )(ASearchRow.apply _)
}

case class ASearchs(aSearchs: List[ASearch])

case class ASearchRows(aSearchRows: List[ASearchRow])