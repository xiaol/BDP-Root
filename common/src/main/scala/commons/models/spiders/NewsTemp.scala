package commons.models.spiders

import commons.models.news.NewsBodyBlock
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhange on 2016-05-05.
 *
 */

// TODO: JSON implicits, conditions virify like `require(url.nonEmpty)` and so on...

case class NewsTemp(
    url: String,
    title: String,
    tags: Option[String],
    author: Option[String],
    ptime: LocalDateTime,
    pname: Option[String],
    purl: Option[String],
    html: String,
    synopsis: Option[String],
    province: Option[String],
    city: Option[String],
    district: Option[String],
    docid: String,
    content: List[NewsBodyBlock],
    channel: Long,
    source: Long,
    sstate: Int,
    pconf: Option[JsValue],
    comment_queue: Option[String],
    comment_task: Option[String]) {

  /**
   * Requirements for validity check.
   */

  require(!url.isEmpty, "url must be non empty.")
  require(!title.isEmpty, "title must be non empty.")
  if (tags.isDefined) require(!tags.get.isEmpty, "tags must be non empty.")
  if (author.isDefined) require(!author.get.isEmpty, "author must be non empty.")
  if (pname.isDefined) require(!pname.get.isEmpty, "pname must be non empty.")
  if (purl.isDefined) require(!purl.get.isEmpty, "purl must be non empty.")
  require(!html.isEmpty, "content_html must be non empty.")
  if (synopsis.isDefined) require(!synopsis.get.isEmpty, "synopsis must be non empty.")
  if (province.isDefined) require(!province.get.isEmpty, "province must be non empty.")
  if (city.isDefined) require(!city.get.isEmpty, "city must be non empty.")
  if (district.isDefined) require(!district.get.isEmpty, "district must be non empty.")
  require(!docid.isEmpty, "docid must be non empty.")
  require(content.nonEmpty, "content must be non empty.")
}

object NewsTemp {
  implicit val NewsTempWrites: Writes[NewsTemp] = (
    (JsPath \ "url").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "tags").writeNullable[String] ~
    (JsPath \ "author").writeNullable[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "html").write[String] ~
    (JsPath \ "synopsis").writeNullable[String] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "district").writeNullable[String] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "content").write[List[NewsBodyBlock]] ~
    (JsPath \ "channel").write[Long] ~
    (JsPath \ "source").write[Long] ~
    (JsPath \ "sstate").write[Int] ~
    (JsPath \ "pconf").writeNullable[JsValue] ~
    (JsPath \ "comment_queue").writeNullable[String] ~
    (JsPath \ "comment_task").writeNullable[String]
  )(unlift(NewsTemp.unapply))

  implicit val NewsTempReads: Reads[NewsTemp] = (
    (JsPath \ "url").read[String](minLength[String](1)) ~
    (JsPath \ "title").read[String](minLength[String](1)) ~
    (JsPath \ "tags").readNullable[String](minLength[String](1)) ~
    (JsPath \ "author").readNullable[String](minLength[String](1)) ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String](minLength[String](1)) ~
    (JsPath \ "purl").readNullable[String](minLength[String](1)) ~
    (JsPath \ "html").read[String](minLength[String](1)) ~
    (JsPath \ "synopsis").readNullable[String](minLength[String](1)) ~
    (JsPath \ "province").readNullable[String](minLength[String](1)) ~
    (JsPath \ "city").readNullable[String](minLength[String](1)) ~
    (JsPath \ "district").readNullable[String](minLength[String](1)) ~
    (JsPath \ "docid").read[String](minLength[String](1)) ~
    (JsPath \ "content").read[List[NewsBodyBlock]] ~
    (JsPath \ "channel").read[Long] ~
    (JsPath \ "source").read[Long] ~
    (JsPath \ "sstate").read[Int] ~
    (JsPath \ "pconf").readNullable[JsValue] ~
    (JsPath \ "comment_queue").readNullable[String](minLength[String](1)) ~
    (JsPath \ "comment_task").readNullable[String](minLength[String](1))
  )(NewsTemp.apply _)
}