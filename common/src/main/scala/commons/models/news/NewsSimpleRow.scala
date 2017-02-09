package commons.models.news

/**
 * Created by zhangshl on 17/1/24.
 */

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class NewsSimpleRowBase(
  nid: Option[Long] = None,
  url: String,
  docid: String,
  title: String,
  author: Option[String] = None,
  ptime: LocalDateTime,
  pname: Option[String] = None,
  purl: Option[String] = None,
  descr: Option[String] = None,
  tags: Option[List[String]] = None)

object NewsSimpleRowBase {
  implicit val NewsSimpleRowBaseWrites: Writes[NewsSimpleRowBase] = (
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "url").write[String] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "author").writeNullable[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "tags").writeNullable[List[String]]
  )(unlift(NewsSimpleRowBase.unapply))

  implicit val NewsSimpleRowBaseReads: Reads[NewsSimpleRowBase] = (
    (JsPath \ "nid").readNullable[Long] ~
    (JsPath \ "url").read[String] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "author").readNullable[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "tags").readNullable[List[String]]
  )(NewsSimpleRowBase.apply _)
}

case class NewsSimpleRowIncr(
  collect: Int,
  concern: Int,
  comment: Int,
  inum: Int,
  style: Int,
  imgs: Option[List[String]] = None)

object NewsSimpleRowIncr {
  implicit val NewsSimpleRowIncrWrites: Writes[NewsSimpleRowIncr] = (
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "inum").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]]
  )(unlift(NewsSimpleRowIncr.unapply))

  implicit val NewsSimpleRowIncrReads: Reads[NewsSimpleRowIncr] = (
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "inum").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]]
  )(NewsSimpleRowIncr.apply _)
}

case class NewsSimpleRowSyst(
  state: Int,
  ctime: LocalDateTime,
  chid: Long,
  sechid: Option[Long],
  icon: Option[String] = None,
  rtype: Option[Int] = None,
  videourl: Option[String] = None,
  thumbnail: Option[String] = None,
  duration: Option[Int] = None)

object NewsSimpleRowSyst {
  implicit val NewsSimpleRowSystWrites: Writes[NewsSimpleRowSyst] = (
    (JsPath \ "state").write[Int] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "chid").write[Long] ~
    (JsPath \ "sechid").writeNullable[Long] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "rtype").writeNullable[Int] ~
    (JsPath \ "videourl").writeNullable[String] ~
    (JsPath \ "thumbnail").writeNullable[String] ~
    (JsPath \ "duration").writeNullable[Int]
  )(unlift(NewsSimpleRowSyst.unapply))

  implicit val NewsSimpleRowSystReads: Reads[NewsSimpleRowSyst] = (
    (JsPath \ "state").read[Int] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "chid").read[Long] ~
    (JsPath \ "sechid").readNullable[Long] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "rtype").readNullable[Int] ~
    (JsPath \ "videourl").readNullable[String] ~
    (JsPath \ "thumbnail").readNullable[String] ~
    (JsPath \ "duration").readNullable[Int]
  )(NewsSimpleRowSyst.apply _)
}

case class NewsSimpleRow(base: NewsSimpleRowBase, incr: NewsSimpleRowIncr, syst: NewsSimpleRowSyst)

object NewsSimpleRow {
  implicit val NewsSimpleRowWrites: Writes[NewsSimpleRow] = (
    (JsPath \ "base").write[NewsSimpleRowBase] ~
    (JsPath \ "incr").write[NewsSimpleRowIncr] ~
    (JsPath \ "syst").write[NewsSimpleRowSyst]
  )(unlift(NewsSimpleRow.unapply))

  implicit val NewsSimpleRowReads: Reads[NewsSimpleRow] = (
    (JsPath \ "base").read[NewsSimpleRowBase] ~
    (JsPath \ "incr").read[NewsSimpleRowIncr] ~
    (JsPath \ "syst").read[NewsSimpleRowSyst]
  )(NewsSimpleRow.apply _)

}
