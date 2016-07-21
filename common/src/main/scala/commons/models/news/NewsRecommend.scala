package commons.models.news

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }
import commons.utils.Joda4PlayJsonImplicits._
import slick.jdbc.GetResult
/**
 * Created by zhangshl on 16/7/15.
 */
case class NewsRecommend(
  nid: Long,
  rtime: Option[LocalDateTime] = None,
  level: Option[Double] = None,
  bigimg: Option[Int] = None,
  status: Option[Int] = None)

object NewsRecommend {
  implicit val NewsRecommendWrites: Writes[NewsRecommend] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "rtime").writeNullable[LocalDateTime] ~
    (JsPath \ "level").writeNullable[Double] ~
    (JsPath \ "bigimg").writeNullable[Int] ~
    (JsPath \ "status").writeNullable[Int]
  )(unlift(NewsRecommend.unapply))

  implicit val NewsRecommendReads: Reads[NewsRecommend] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "rtime").readNullable[LocalDateTime] ~
    (JsPath \ "level").readNullable[Double] ~
    (JsPath \ "bigimg").readNullable[Int] ~
    (JsPath \ "status").readNullable[Int]
  )(NewsRecommend.apply _)
}

case class NewsRecommendResponse(
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
  district: Option[String] = None,

  rtime: Option[LocalDateTime] = None,
  level: Option[Double] = None,
  bigimg: Option[Int] = None,
  status: Option[Int] = None)

object NewsRecommendResponse {

  implicit val NewsRecommendResponseWrites: Writes[NewsRecommendResponse] = (
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
    (JsPath \ "district").writeNullable[String] ~

    (JsPath \ "rtime").writeNullable[LocalDateTime] ~
    (JsPath \ "level").writeNullable[Double] ~
    (JsPath \ "bigimg").writeNullable[Int] ~
    (JsPath \ "status").writeNullable[Int]
  )(unlift(NewsRecommendResponse.unapply))

  implicit val NewsRecommendResponseReads: Reads[NewsRecommendResponse] = (
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
    (JsPath \ "district").readNullable[String] ~

    (JsPath \ "rtime").readNullable[LocalDateTime] ~
    (JsPath \ "level").readNullable[Double] ~
    (JsPath \ "bigimg").readNullable[Int] ~
    (JsPath \ "status").readNullable[Int]
  )(NewsRecommendResponse.apply _)

  def from(newsFeedResponse: NewsFeedResponse): NewsRecommendResponse = {
    NewsRecommendResponse(newsFeedResponse.nid, newsFeedResponse.docid, newsFeedResponse.title, newsFeedResponse.ptime, newsFeedResponse.pname, newsFeedResponse.purl, newsFeedResponse.descr, newsFeedResponse.channel, newsFeedResponse.collect, newsFeedResponse.concern, newsFeedResponse.comment, newsFeedResponse.style, newsFeedResponse.imgs, newsFeedResponse.province, newsFeedResponse.city, newsFeedResponse.district)
  }
  def from(newsFeedResponse: NewsFeedResponse, newsRecommend: NewsRecommend): NewsRecommendResponse = {
    NewsRecommendResponse(newsFeedResponse.nid, newsFeedResponse.docid, newsFeedResponse.title, newsFeedResponse.ptime, newsFeedResponse.pname, newsFeedResponse.purl, newsFeedResponse.descr, newsFeedResponse.channel, newsFeedResponse.collect, newsFeedResponse.concern, newsFeedResponse.comment, newsFeedResponse.style, newsFeedResponse.imgs, newsFeedResponse.province, newsFeedResponse.city, newsFeedResponse.district, newsRecommend.rtime, newsRecommend.level, newsRecommend.bigimg, newsRecommend.status)
  }
}