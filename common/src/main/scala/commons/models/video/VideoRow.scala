package commons.models.video

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class VideoRowBase(
  nid: Option[Long] = None,
  url: String,
  videourl: String,
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

object VideoRowBase {
  implicit val VideoRowBaseWrites: Writes[VideoRowBase] = (
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "url").write[String] ~
    (JsPath \ "videourl").write[String] ~
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
  )(unlift(VideoRowBase.unapply))

  implicit val VideoRowBaseReads: Reads[VideoRowBase] = (
    (JsPath \ "nid").readNullable[Long] ~
    (JsPath \ "url").read[String] ~
    (JsPath \ "videourl").read[String] ~
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
  )(VideoRowBase.apply _)
}

case class VideoRowIncr(
  collect: Int,
  concern: Int,
  comment: Int,
  inum: Int,
  style: Int,
  imgs: Option[List[String]] = None,
  compress: Option[String] = None,
  ners: Option[JsValue] = None)

object VideoRowIncr {
  implicit val VideoRowIncrWrites: Writes[VideoRowIncr] = (
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "inum").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "compress").writeNullable[String] ~
    (JsPath \ "ners").writeNullable[JsValue]
  )(unlift(VideoRowIncr.unapply))

  implicit val VideoRowIncrReads: Reads[VideoRowIncr] = (
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "inum").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "compress").readNullable[String] ~
    (JsPath \ "ners").readNullable[JsValue]
  )(VideoRowIncr.apply _)
}

case class VideoRowSyst(
  state: Int,
  ctime: LocalDateTime,
  chid: Long,
  sechid: Option[Long],
  srid: Long,
  srstate: Int,
  pconf: Option[JsValue] = None,
  plog: Option[JsValue] = None,
  icon: Option[String] = None,
  thumbnail: String)

object VideoRowSyst {
  implicit val VideoRowSystWrites: Writes[VideoRowSyst] = (
    (JsPath \ "state").write[Int] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "chid").write[Long] ~
    (JsPath \ "sechid").writeNullable[Long] ~
    (JsPath \ "srid").write[Long] ~
    (JsPath \ "srstate").write[Int] ~
    (JsPath \ "pconf").writeNullable[JsValue] ~
    (JsPath \ "plog").writeNullable[JsValue] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "thumbnail").write[String]
  )(unlift(VideoRowSyst.unapply))

  implicit val VideoRowSystReads: Reads[VideoRowSyst] = (
    (JsPath \ "state").read[Int] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "chid").read[Long] ~
    (JsPath \ "sechid").readNullable[Long] ~
    (JsPath \ "srid").read[Long] ~
    (JsPath \ "srstate").read[Int] ~
    (JsPath \ "pconf").readNullable[JsValue] ~
    (JsPath \ "plog").readNullable[JsValue] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "thumbnail").read[String]
  )(VideoRowSyst.apply _)
}

case class VideoRow(base: VideoRowBase, incr: VideoRowIncr, syst: VideoRowSyst)

object VideoRow {
  implicit val VideoRowWrites: Writes[VideoRow] = (
    (JsPath \ "base").write[VideoRowBase] ~
    (JsPath \ "incr").write[VideoRowIncr] ~
    (JsPath \ "syst").write[VideoRowSyst]
  )(unlift(VideoRow.unapply))

  implicit val VideoRowReads: Reads[VideoRow] = (
    (JsPath \ "base").read[VideoRowBase] ~
    (JsPath \ "incr").read[VideoRowIncr] ~
    (JsPath \ "syst").read[VideoRowSyst]
  )(VideoRow.apply _)

}