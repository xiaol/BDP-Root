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
  content: JsValue,
  html: String,
  author: Option[String] = None,
  ptime: LocalDateTime,
  pname: Option[String] = None,
  purl: Option[String] = None,
  descr: Option[String] = None,
  tags: Option[List[String]] = None,
  province: Option[String] = None,
  city: Option[String] = None,
  district: Option[String] = None)

object NewsRowBase {
  implicit val NewsRowBaseWrites: Writes[NewsRowBase] = (
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "url").write[String] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "content").write[JsValue] ~
    (JsPath \ "html").write[String] ~
    (JsPath \ "author").writeNullable[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "tags").writeNullable[List[String]] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "district").writeNullable[String]
  )(unlift(NewsRowBase.unapply))

  implicit val NewsRowBaseReads: Reads[NewsRowBase] = (
    (JsPath \ "nid").readNullable[Long] ~
    (JsPath \ "url").read[String] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "content").read[JsValue] ~
    (JsPath \ "html").read[String] ~
    (JsPath \ "author").readNullable[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "tags").readNullable[List[String]] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String]
  )(NewsRowBase.apply _)
}

case class NewsRowIncr(
  collect: Int,
  concern: Int,
  comment: Int,
  inum: Int,
  style: Int,
  imgs: Option[List[String]] = None,
  compress: Option[String] = None,
  ners: Option[JsValue] = None)

object NewsRowIncr {
  implicit val NewsRowIncrWrites: Writes[NewsRowIncr] = (
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "inum").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "compress").writeNullable[String] ~
    (JsPath \ "ners").writeNullable[JsValue]
  )(unlift(NewsRowIncr.unapply))

  implicit val NewsRowIncrReads: Reads[NewsRowIncr] = (
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "inum").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "compress").readNullable[String] ~
    (JsPath \ "ners").readNullable[JsValue]
  )(NewsRowIncr.apply _)
}

case class NewsRowSyst(
  state: Int,
  ctime: LocalDateTime,
  channel: Long,
  source: Long,
  sstate: Int,
  pconf: Option[JsValue] = None,
  plog: Option[JsValue] = None)

object NewsRowSyst {
  implicit val NewsRowSystWrites: Writes[NewsRowSyst] = (
    (JsPath \ "state").write[Int] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "channel").write[Long] ~
    (JsPath \ "source").write[Long] ~
    (JsPath \ "sstate").write[Int] ~
    (JsPath \ "pconf").writeNullable[JsValue] ~
    (JsPath \ "plog").writeNullable[JsValue]
  )(unlift(NewsRowSyst.unapply))

  implicit val NewsRowSystReads: Reads[NewsRowSyst] = (
    (JsPath \ "state").read[Int] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "channel").read[Long] ~
    (JsPath \ "source").read[Long] ~
    (JsPath \ "sstate").read[Int] ~
    (JsPath \ "pconf").readNullable[JsValue] ~
    (JsPath \ "plog").readNullable[JsValue]
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
