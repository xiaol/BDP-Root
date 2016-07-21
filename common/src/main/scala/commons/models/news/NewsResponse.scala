package commons.models.news

import com.sksamuel.elastic4s.{ HitAs, RichSearchHit }
import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class NewsFeedResponse(
  nid: Long,
  docid: String,
  title: String,
  ptime: LocalDateTime,
  pname: Option[String] = None,
  purl: Option[String] = None,
  descr: Option[String] = None,
  channel: Long,
  collect: Int,
  concern: Int,
  comment: Int,
  style: Int,
  imgs: Option[List[String]] = None,
  province: Option[String] = None,
  city: Option[String] = None,
  district: Option[String] = None)

object NewsFeedResponse {

  implicit object NewsFeedResponseHitAs extends HitAs[NewsFeedResponse] {
    override def as(hit: RichSearchHit): NewsFeedResponse = {

      val newsFeedResponse: NewsFeedResponse = Json.parse(hit.sourceAsString).as[NewsFeedResponse]
      val highlight = hit.java.getHighlightFields
      if (!highlight.isEmpty) {
        newsFeedResponse.copy(title = highlight.get("title").fragments()(0).toString)
      } else newsFeedResponse
    }
  }

  implicit val NewsFeedResponseWrites: Writes[NewsFeedResponse] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "channel").write[Long] ~
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "district").writeNullable[String]
  )(unlift(NewsFeedResponse.unapply))

  implicit val NewsFeedResponseReads: Reads[NewsFeedResponse] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "channel").read[Long] ~
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String]
  )(NewsFeedResponse.apply _)

  def from(newsRow: NewsRow): NewsFeedResponse = {
    val base = newsRow.base
    val incr = newsRow.incr
    val syst = newsRow.syst
    NewsFeedResponse(base.nid.get, base.docid, base.title, syst.ctime, base.pname, base.purl, base.descr, syst.channel, incr.collect, incr.concern, incr.comment, incr.style, incr.imgs, base.province, base.city, base.district)
  }
}

case class NewsDetailsResponse(
  nid: Long,
  docid: String,
  title: String,
  ptime: LocalDateTime,
  pname: Option[String] = None,
  purl: Option[String] = None,
  channel: Long,
  inum: Int,
  tags: Option[List[String]] = None,
  descr: Option[String] = None,
  content: JsValue,
  collect: Int,
  concern: Int,
  comment: Int,
  colFlag: Option[Int],
  conFlag: Option[Int],
  conPubFlag: Option[Int])

object NewsDetailsResponse {
  implicit val NewsDetailsResponseWrites: Writes[NewsDetailsResponse] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "channel").write[Long] ~
    (JsPath \ "inum").write[Int] ~
    (JsPath \ "tags").writeNullable[List[String]] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "content").write[JsValue] ~
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "colflag").writeNullable[Int] ~
    (JsPath \ "conflag").writeNullable[Int] ~
    (JsPath \ "conpubflag").writeNullable[Int]
  )(unlift(NewsDetailsResponse.unapply))

  implicit val NewsDetailsResponseReads: Reads[NewsDetailsResponse] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "channel").read[Long] ~
    (JsPath \ "inum").read[Int] ~
    (JsPath \ "tags").readNullable[List[String]] ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "content").read[JsValue] ~
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "colflag").readNullable[Int] ~
    (JsPath \ "conflag").readNullable[Int] ~
    (JsPath \ "conpubflag").readNullable[Int]
  )(NewsDetailsResponse.apply _)

  def from(newsRow: NewsRow, colFlag: Option[Int] = None, conFlag: Option[Int] = None, conPubFlag: Option[Int] = None): NewsDetailsResponse = {
    val base = newsRow.base
    val incr = newsRow.incr
    val syst = newsRow.syst
    NewsDetailsResponse(base.nid.get, base.docid, base.title, syst.ctime, base.pname, base.purl, syst.channel, incr.inum, base.tags, base.descr, base.content, incr.collect, incr.concern, incr.comment, colFlag, conFlag, conPubFlag)
  }
}
