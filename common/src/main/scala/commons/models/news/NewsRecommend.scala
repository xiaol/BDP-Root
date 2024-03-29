package commons.models.news

import java.sql.Date

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
  concern: Int,
  comment: Int,
  style: Int,
  imgs: Option[List[String]] = None,
  rtime: Option[LocalDateTime] = None,
  level: Option[Double] = None,
  bigimg: Option[Int] = None,
  status: Option[Int] = None,

  showcount: Option[Int] = None,
  clickcount: Option[Int] = None)

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
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "rtime").writeNullable[LocalDateTime] ~
    (JsPath \ "level").writeNullable[Double] ~
    (JsPath \ "bigimg").writeNullable[Int] ~
    (JsPath \ "status").writeNullable[Int] ~

    (JsPath \ "showcount").writeNullable[Int] ~
    (JsPath \ "clickcount").writeNullable[Int]
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
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "rtime").readNullable[LocalDateTime] ~
    (JsPath \ "level").readNullable[Double] ~
    (JsPath \ "bigimg").readNullable[Int] ~
    (JsPath \ "status").readNullable[Int] ~

    (JsPath \ "showcount").readNullable[Int] ~
    (JsPath \ "clickcount").readNullable[Int]
  )(NewsRecommendResponse.apply _)

  def from(newsFeedResponse: NewsFeedResponse): NewsRecommendResponse = {
    NewsRecommendResponse(newsFeedResponse.nid, newsFeedResponse.docid, newsFeedResponse.title, newsFeedResponse.ptime, newsFeedResponse.pname, newsFeedResponse.purl, None, newsFeedResponse.channel, newsFeedResponse.concern, newsFeedResponse.comment, newsFeedResponse.style, newsFeedResponse.imgs) //, newsFeedResponse.province, newsFeedResponse.city, newsFeedResponse.district)
  }
  def from(newsFeedResponse: NewsFeedResponse, newsRecommend: NewsRecommend): NewsRecommendResponse = {
    NewsRecommendResponse(newsFeedResponse.nid, newsFeedResponse.docid, newsFeedResponse.title, newsFeedResponse.ptime, newsFeedResponse.pname, newsFeedResponse.purl, None, newsFeedResponse.channel, newsFeedResponse.concern, newsFeedResponse.comment, newsFeedResponse.style, newsFeedResponse.imgs, //newsFeedResponse.province, newsFeedResponse.city, newsFeedResponse.district,
      newsRecommend.rtime, newsRecommend.level, newsRecommend.bigimg, newsRecommend.status)
  }
}

case class NewsRecommendRead(
  uid: Long,
  nid: Long,
  readtime: LocalDateTime,
  logtype: Option[Int] = None,
  logchid: Option[Int] = None)

object NewsRecommendRead {
  implicit val NewsRecommendReadWrites: Writes[NewsRecommendRead] = (
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "readtime").write[LocalDateTime] ~
    (JsPath \ "logtype").writeNullable[Int] ~
    (JsPath \ "logchid").writeNullable[Int]
  )(unlift(NewsRecommendRead.unapply))

  implicit val NewsRecommendReadReads: Reads[NewsRecommendRead] = (
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "readtime").read[LocalDateTime] ~
    (JsPath \ "logtype").readNullable[Int] ~
    (JsPath \ "logchid").readNullable[Int]
  )(NewsRecommendRead.apply _)
}

case class NewsRecommendForUser(
  uid: Long,
  nid: Long,
  predict: Double,
  ctime: LocalDateTime,
  sourcetype: Int)

object NewsRecommendForUser {
  implicit val NewsRecommendForUserWrites: Writes[NewsRecommendForUser] = (
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "predict").write[Double] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "sourcetype").write[Int]
  )(unlift(NewsRecommendForUser.unapply))

  implicit val NewsRecommendForUserReads: Reads[NewsRecommendForUser] = (
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "predict").read[Double] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "sourcetype").read[Int]
  )(NewsRecommendForUser.apply _)
}

case class NewsRecommendHot(
  nid: Long,
  ctime: LocalDateTime,
  status: Option[Int] = None)

object NewsRecommendHot {
  implicit val NewsRecommendHotWrites: Writes[NewsRecommendHot] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "readtime").write[LocalDateTime] ~
    (JsPath \ "status").writeNullable[Int]
  )(unlift(NewsRecommendHot.unapply))

  implicit val NewsRecommendHotReads: Reads[NewsRecommendHot] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "readtime").read[LocalDateTime] ~
    (JsPath \ "status").readNullable[Int]
  )(NewsRecommendHot.apply _)
}

case class Newsrecommendclick(
  uid: Long,
  nid: Long,
  ctime: Date)

object Newsrecommendclick {
  implicit val getNewsrecommendclickResult = GetResult(r => Newsrecommendclick(r.<<, r.<<, r.<<))

  implicit val NewsrecommendclickWrites: Writes[Newsrecommendclick] = (
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "ctime").write[Date]
  )(unlift(Newsrecommendclick.unapply))

  implicit val NewsrecommendclickReads: Reads[Newsrecommendclick] = (
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "ctime").read[Date]
  )(Newsrecommendclick.apply _)
}

case class NewsClick(
  uid: Long,
  nid: Long,
  ctime: LocalDateTime)

object NewsClick {

  implicit val NewsClickWrites: Writes[NewsClick] = (
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "ctime").write[LocalDateTime]
  )(unlift(NewsClick.unapply))

  implicit val NewsClickReads: Reads[NewsClick] = (
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "ctime").read[LocalDateTime]
  )(NewsClick.apply _)
}

case class NewsRecommendLike(id: Option[Long] = None,
                             uid: Long,
                             nid: Long,
                             predict: Double,
                             ctime: LocalDateTime)

object NewsRecommendLike {
  implicit val NewsRecommendLikeWrites: Writes[NewsRecommendLike] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "predict").write[Double] ~
    (JsPath \ "ctime").write[LocalDateTime]
  )(unlift(NewsRecommendLike.unapply))

  implicit val NewsRecommendLikeReads: Reads[NewsRecommendLike] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "predict").read[Double] ~
    (JsPath \ "ctime").read[LocalDateTime]
  )(NewsRecommendLike.apply _)
}

case class PvDetail(uid: Long,
                    method: String,
                    ctime: LocalDateTime,
                    ipaddress: Option[String] = None)

object PvDetail {
  implicit val PvDetailWrites: Writes[PvDetail] = (
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "method").write[String] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "ipaddress").writeNullable[String]
  )(unlift(PvDetail.unapply))

  implicit val PvDetailReads: Reads[PvDetail] = (
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "method").read[String] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "ipaddress").readNullable[String]
  )(PvDetail.apply _)
}