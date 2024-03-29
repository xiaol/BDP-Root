package commons.models.news

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class NewsRowBase(
  nid: Option[Long] = None,
  url: String,
  docid: String,
  title: String,
  author: Option[String] = None,
  ptime: LocalDateTime,
  pname: Option[String] = None,
  purl: Option[String] = None,
  province: Option[String] = None,
  city: Option[String] = None,
  district: Option[String] = None)

object NewsRowBase {
  implicit val NewsRowBaseWrites: Writes[NewsRowBase] = (
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "url").write[String] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "author").writeNullable[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "district").writeNullable[String]
  )(unlift(NewsRowBase.unapply))

  implicit val NewsRowBaseReads: Reads[NewsRowBase] = (
    (JsPath \ "nid").readNullable[Long] ~
    (JsPath \ "url").read[String] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "author").readNullable[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String]
  )(NewsRowBase.apply _)
}

case class NewsRowIncr(
  collect: Int,
  concern: Int,
  un_concern: Option[Int] = None,
  comment: Int,
  inum: Int,
  style: Int,
  imgs: Option[List[String]] = None)

object NewsRowIncr {
  implicit val NewsRowIncrWrites: Writes[NewsRowIncr] = (
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "un_concern").writeNullable[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "inum").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]]
  )(unlift(NewsRowIncr.unapply))

  implicit val NewsRowIncrReads: Reads[NewsRowIncr] = (
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "un_concern").readNullable[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "inum").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]]
  )(NewsRowIncr.apply _)
}

case class NewsRowSyst(
  state: Int,
  ctime: LocalDateTime,
  chid: Long,
  sechid: Option[Long],
  srid: Long,
  icon: Option[String] = None,
  rtype: Option[Int] = None,
  videourl: Option[String] = None,
  thumbnail: Option[String] = None,
  duration: Option[Int] = None)

object NewsRowSyst {
  implicit val NewsRowSystWrites: Writes[NewsRowSyst] = (
    (JsPath \ "state").write[Int] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "chid").write[Long] ~
    (JsPath \ "sechid").writeNullable[Long] ~
    (JsPath \ "srid").write[Long] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "rtype").writeNullable[Int] ~
    (JsPath \ "videourl").writeNullable[String] ~
    (JsPath \ "thumbnail").writeNullable[String] ~
    (JsPath \ "duration").writeNullable[Int]
  )(unlift(NewsRowSyst.unapply))

  implicit val NewsRowSystReads: Reads[NewsRowSyst] = (
    (JsPath \ "state").read[Int] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "chid").read[Long] ~
    (JsPath \ "sechid").readNullable[Long] ~
    (JsPath \ "srid").read[Long] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "rtype").readNullable[Int] ~
    (JsPath \ "videourl").readNullable[String] ~
    (JsPath \ "thumbnail").readNullable[String] ~
    (JsPath \ "duration").readNullable[Int]
  )(NewsRowSyst.apply _)
}

case class NewsRow(base: NewsRowBase, incr: NewsRowIncr, syst: NewsRowSyst)

object NewsRow {
  implicit val NewsRowWrites: Writes[NewsRow] = (
    (JsPath \ "base").write[NewsRowBase] ~
    (JsPath \ "incr").write[NewsRowIncr] ~
    (JsPath \ "syst").write[NewsRowSyst]
  )(unlift(NewsRow.unapply))

  implicit val NewsRowReads: Reads[NewsRow] = (
    (JsPath \ "base").read[NewsRowBase] ~
    (JsPath \ "incr").read[NewsRowIncr] ~
    (JsPath \ "syst").read[NewsRowSyst]
  )(NewsRow.apply _)

}
