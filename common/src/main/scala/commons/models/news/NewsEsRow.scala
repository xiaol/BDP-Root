package commons.models.news

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ Reads, _ }
import commons.utils.Joda4PlayJsonImplicits._

/**
 * Created by zhange on 2016-06-28.
 *
 */

case class NewsEsRow(
  nid: Option[Long] = None,
  docid: String,
  title: String,
  tags: Option[List[String]] = None,
  descr: Option[String] = None,
  province: Option[String] = None,
  city: Option[String] = None,
  district: Option[String] = None,
  ptime: LocalDateTime,
  pname: Option[String] = None,
  purl: Option[String] = None,
  channel: Long,
  collect: Int,
  concern: Int,
  comment: Int,
  style: Int,
  imgs: Option[List[String]] = None,
  compress: Option[String] = None,
  ners: Option[JsValue] = None,
  ctime: LocalDateTime)

object NewsEsRow {
  implicit val NewsEsRowWrites: Writes[NewsEsRow] = (
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "tags").writeNullable[List[String]] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "district").writeNullable[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "channel").write[Long] ~
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "compress").writeNullable[String] ~
    (JsPath \ "ners").writeNullable[JsValue] ~
    (JsPath \ "ctime").write[LocalDateTime]
  )(unlift(NewsEsRow.unapply))

  implicit val NewsEsRowReads: Reads[NewsEsRow] = (
    (JsPath \ "nid").readNullable[Long] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "tags").readNullable[List[String]] ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "channel").read[Long] ~
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "compress").readNullable[String] ~
    (JsPath \ "ners").readNullable[JsValue] ~
    (JsPath \ "ctime").read[LocalDateTime]
  )(NewsEsRow.apply _)

  def from(row: NewsRow): NewsEsRow = {
    val base = row.base
    val incr = row.incr
    val syst = row.syst
    NewsEsRow(base.nid, base.docid, base.title, base.tags, base.descr, base.province, base.city, base.district, base.ptime, base.pname, base.purl, syst.chid, incr.collect, incr.concern, incr.comment, incr.style, incr.imgs, incr.compress, incr.ners, syst.ctime)
  }
}

//搜索新闻带上订阅号
case class NewsFeedWithPublisherWithUserInfoResponse(news: Seq[NewsFeedResponse], total: Long, publisher: Option[Seq[NewsPublisherWithUserResponse]], s: Option[String])

object NewsFeedWithPublisherWithUserInfoResponse {
  implicit val NewsFeedWithPublisherWithUserInfoResponseWrites: Writes[NewsFeedWithPublisherWithUserInfoResponse] = (
    (JsPath \ "news").write[Seq[NewsFeedResponse]] ~
    (JsPath \ "total").write[Long] ~
    (JsPath \ "publisher").writeNullable[Seq[NewsPublisherWithUserResponse]] ~
    (JsPath \ "s").writeNullable[String]
  )(unlift(NewsFeedWithPublisherWithUserInfoResponse.unapply))

  def apply(news: Seq[NewsFeedResponse], t: Long, p: Option[Seq[NewsPublisherWithUserResponse]]): NewsFeedWithPublisherWithUserInfoResponse = new NewsFeedWithPublisherWithUserInfoResponse(news, t, p, None)
}

//订阅号信息及用户是否关注该订阅号
case class NewsPublisherWithUserResponse(id: Option[Long] = None,
                                         ctime: LocalDateTime,
                                         name: String,
                                         icon: Option[String] = None,
                                         descr: Option[String] = None,
                                         concern: Int = 0,
                                         flag: Long)

object NewsPublisherWithUserResponse {
  implicit val NewsPublisherRowWrites: Writes[NewsPublisherWithUserResponse] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "name").write[String] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "flag").write[Long]
  )(unlift(NewsPublisherWithUserResponse.unapply))

  implicit val NewsPublisherRowReads: Reads[NewsPublisherWithUserResponse] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "name").read[String] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "flag").read[Long]
  )(NewsPublisherWithUserResponse.apply _)
}