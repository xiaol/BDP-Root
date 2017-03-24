package commons.models.news

import java.sql.Timestamp

import play.api.libs.functional.syntax._
import play.api.libs.json._
import commons.utils.Joda4PlayJsonImplicits._
import slick.jdbc.GetResult

/**
 * Created by zhangshl on 17/3/23.
 */
case class NewsFeedRow(
  nid: Long,
  docid: String,
  title: String,
  pname: Option[String] = None,
  purl: Option[String] = None,
  chid: Long,
  collect: Int,
  concern: Int,
  un_concern: Option[Int] = None,
  comment: Int,
  style: Int,
  imgs: Option[String] = None,
  icon: Option[String] = None,
  videourl: Option[String] = None,
  duration: Option[Int] = None,
  thumbnail: Option[String] = None,
  rtype: Option[Int] = None,
  logtype: Option[Int] = Some(0))

object NewsFeedRow {
  implicit val getNewsFeedRowResult = GetResult(r => NewsFeedRow(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  implicit val NewsFeedRowWrites: Writes[NewsFeedRow] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "chid").write[Long] ~
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "un_concern").writeNullable[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[String] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "videourl").writeNullable[String] ~
    (JsPath \ "duration").writeNullable[Int] ~
    (JsPath \ "thumbnail").writeNullable[String] ~
    (JsPath \ "rtype").writeNullable[Int] ~
    (JsPath \ "logtype").writeNullable[Int]
  )(unlift(NewsFeedRow.unapply))

  implicit val NewsFeedRowReads: Reads[NewsFeedRow] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "channel").read[Long] ~
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "un_concern").readNullable[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[String] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "videourl").readNullable[String] ~
    (JsPath \ "duration").readNullable[Int] ~
    (JsPath \ "thumbnail").readNullable[String] ~
    (JsPath \ "rtype").readNullable[Int] ~
    (JsPath \ "logtype").readNullable[Int]
  )(NewsFeedRow.apply _)

}