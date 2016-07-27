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

case class BaseTemp(
    url: String,
    title: String,
    tags: Option[String],
    author: Option[String],
    ptime: LocalDateTime,
    pname: Option[String],
    purl: Option[String],
    picon: Option[String],
    pdescr: Option[String],
    html: String,
    synopsis: Option[String],
    province: Option[String],
    city: Option[String],
    district: Option[String],
    docid: String,
    content: List[NewsBodyBlock]) {

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

case class SystTemp(
  chid: Long,
  sechid: Option[Long],
  srid: Long,
  srstate: Int,
  pconf: Option[JsValue],
  comment_queue: Option[String],
  comment_task: Option[String])

case class NewsTemp(base: BaseTemp, syst: SystTemp)
