package commons.models.news

import com.sksamuel.elastic4s.{ HitAs, RichSearchHit }
import commons.models.advertisement.{ AdResponse, App, Creative, Adspace }
import commons.models.detail.DetailRow
import commons.models.video.VideoRow
import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.{ Random, Try }

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
  channel: Long,
  concern: Int,
  un_concern: Option[Int] = None,
  comment: Int,
  style: Int,
  imgs: Option[List[String]] = None,
  rtype: Option[Int] = None,
  adimpression: Option[List[String]] = None,
  icon: Option[String] = None,
  videourl: Option[String] = None,
  thumbnail: Option[String] = None,
  duration: Option[Int] = None,
  adresponse: Option[AdResponse] = None,
  logtype: Option[Int] = Some(0),
  logchid: Option[Int] = None,
  extendData: Option[ExtendData] = None)

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
    (JsPath \ "channel").write[Long] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "un_concern").writeNullable[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "rtype").writeNullable[Int] ~
    (JsPath \ "adimpression").writeNullable[List[String]] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "videourl").writeNullable[String] ~
    (JsPath \ "thumbnail").writeNullable[String] ~
    (JsPath \ "duration").writeNullable[Int] ~
    (JsPath \ "adresponse").writeNullable[AdResponse] ~
    (JsPath \ "logtype").writeNullable[Int] ~
    (JsPath \ "logchid").writeNullable[Int] ~
    (JsPath \ "extendData").writeNullable[ExtendData]
  )(unlift(NewsFeedResponse.unapply))

  implicit val NewsFeedResponseReads: Reads[NewsFeedResponse] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "channel").read[Long] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "un_concern").readNullable[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "rtype").readNullable[Int] ~
    (JsPath \ "adimpression").readNullable[List[String]] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "videourl").readNullable[String] ~
    (JsPath \ "thumbnail").readNullable[String] ~
    (JsPath \ "duration").readNullable[Int] ~
    (JsPath \ "adresponse").readNullable[AdResponse] ~
    (JsPath \ "logtype").readNullable[Int] ~
    (JsPath \ "logchid").readNullable[Int] ~
    (JsPath \ "extendData").readNullable[ExtendData]
  )(NewsFeedResponse.apply _)

  def from(newsRow: NewsRow): NewsFeedResponse = {
    val base = newsRow.base
    val incr = newsRow.incr
    val syst = newsRow.syst
    //修改评论数
    var commentnum = incr.comment
    if (commentnum > 10 && commentnum <= 70) {
      commentnum = commentnum * 2
    } else if (commentnum > 70 && commentnum <= 200) {
      commentnum = commentnum * 13
    } else if (commentnum > 200) {
      commentnum = commentnum * 61
    }
    NewsFeedResponse(base.nid.get, base.docid, base.title, syst.ctime, base.pname, base.purl,
      syst.chid, incr.concern, incr.un_concern, commentnum, incr.style, incr.imgs,
      syst.rtype, None, syst.icon, syst.videourl, syst.thumbnail, syst.duration)
  }

  def from(videoRow: VideoRow): NewsFeedResponse = {
    val base = videoRow.base
    val incr = videoRow.incr
    val syst = videoRow.syst
    //修改评论数
    var commentnum = incr.comment
    if (commentnum > 10 && commentnum <= 70) {
      commentnum = commentnum * 2
    } else if (commentnum > 70 && commentnum <= 200) {
      commentnum = commentnum * 13
    } else if (commentnum > 200) {
      commentnum = commentnum * 61
    }
    NewsFeedResponse(base.nid.get, base.docid, base.title, syst.ctime, base.pname, base.purl,
      syst.chid, incr.concern, incr.un_concern, commentnum, incr.style, incr.imgs,
      Some(6), None, syst.icon, syst.videourl, syst.thumbnail, syst.duration, None, Some(6))
  }

  def from(creative: Creative): NewsFeedResponse = {
    val event = creative.event.get.head
    val ad_native = creative.ad_native.get
    val imgs: Option[List[String]] = Try(ad_native.filter(_.required_field.get == 2).filter(_.index_value.get == "image").map(_.required_value.get) ++: ad_native.filter(_.required_field.get == 2).filter(_.index_value.get == "image").map(_.required_value.get)).toOption
    val icon: Option[String] = Try(ad_native.filter(_.required_field.get == 2).filter(_.index_value.get == "icon").map(_.required_value.get).head).toOption

    //    val imgs: Option[List[String]] = Try(ad_native.filter(_.required_field.get == 2).map(_.required_value.get)).toOption
    val number: Option[Int] = imgs.map(_.size).map { num =>
      if (num > 3)
        3
      else if (num == 2)
        1
      else
        num
    }
    val title: String = Try(ad_native.filter(_.required_field.get == 1).filter(_.index_value.get == "description").head.required_value.get).toOption.getOrElse("")
    val pname: Option[String] = Try(ad_native.filter(_.required_field.get == 1).filter(_.index_value.get == "title").head.required_value.get).toOption
    val style = Random.nextInt(2) match {
      case 1 => 1
      case _ => 11
    }

    NewsFeedResponse(creative.cid.get.toLong, creative.cid.get.toString, title, LocalDateTime.now(), pname, event.event_value, 9999L, 0, Some(0), 0, style, imgs,
      Some(3), creative.impression, icon)
  }

  def from(topic: TopicList): NewsFeedResponse = {
    val rtype = topic.top match {
      case 1 => 41 //置顶
      case _ => 4 //普通专题
    }
    NewsFeedResponse(topic.id, "", topic.name, topic.create_time.getOrElse(LocalDateTime.now()), Some(" "), None, 9999L, 0, Some(0), 0, 5, Some(List(topic.cover)),
      Some(rtype), None, None, None, None, None, None, Some(4), Some(1))
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
  inum: Option[Int] = None,
  tags: Option[List[String]] = None,
  descr: Option[String] = None,
  content: Option[JsValue] = None,
  collect: Int,
  concern: Int,
  comment: Int,
  icon: Option[String] = None,
  colFlag: Option[Int],
  conFlag: Option[Int],
  conPubFlag: Option[Int],
  videourl: Option[String] = None,
  thumbnail: Option[String] = None)

object NewsDetailsResponse {
  implicit val NewsDetailsResponseWrites: Writes[NewsDetailsResponse] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "channel").write[Long] ~
    (JsPath \ "inum").writeNullable[Int] ~
    (JsPath \ "tags").writeNullable[List[String]] ~
    (JsPath \ "descr").writeNullable[String] ~
    (JsPath \ "content").writeNullable[JsValue] ~
    (JsPath \ "collect").write[Int] ~
    (JsPath \ "concern").write[Int] ~
    (JsPath \ "comment").write[Int] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "colflag").writeNullable[Int] ~
    (JsPath \ "conflag").writeNullable[Int] ~
    (JsPath \ "conpubflag").writeNullable[Int] ~
    (JsPath \ "videourl").writeNullable[String] ~
    (JsPath \ "thumbnail").writeNullable[String]
  )(unlift(NewsDetailsResponse.unapply))

  implicit val NewsDetailsResponseReads: Reads[NewsDetailsResponse] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "channel").read[Long] ~
    (JsPath \ "inum").readNullable[Int] ~
    (JsPath \ "tags").readNullable[List[String]] ~
    (JsPath \ "descr").readNullable[String] ~
    (JsPath \ "content").readNullable[JsValue] ~
    (JsPath \ "collect").read[Int] ~
    (JsPath \ "concern").read[Int] ~
    (JsPath \ "comment").read[Int] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "colflag").readNullable[Int] ~
    (JsPath \ "conflag").readNullable[Int] ~
    (JsPath \ "conpubflag").readNullable[Int] ~
    (JsPath \ "videourl").readNullable[String] ~
    (JsPath \ "thumbnail").readNullable[String]
  )(NewsDetailsResponse.apply _)

  def from(newsFeedRow: NewsRow, detailRow: DetailRow, colFlag: Option[Int] = None, conFlag: Option[Int] = None, conPubFlag: Option[Int] = None): NewsDetailsResponse = {
    val base = newsFeedRow.base
    val syst = newsFeedRow.syst
    val incr = newsFeedRow.incr
    //修改评论数
    var commentnum = incr.comment
    if (commentnum > 10 && commentnum <= 70) {
      commentnum = commentnum * 2
    } else if (commentnum > 70 && commentnum <= 200) {
      commentnum = commentnum * 13
    } else if (commentnum > 200) {
      commentnum = commentnum * 61
    }

    val newsDetailRow = newsFeedRow.syst.rtype match {
      case Some(rtype) if rtype == 6 =>

        val detailbase = detailRow.video.get.base
        val detailsyst = detailRow.video.get.syst
        NewsDetailsResponse(base.nid.get, base.docid, base.title, syst.ctime, base.pname, base.purl, syst.chid, Some(incr.inum), detailbase.tags, None, detailbase.content, incr.collect, incr.concern, commentnum, syst.icon, colFlag, conFlag, conPubFlag, syst.videourl, syst.thumbnail)

      case Some(rtype) if rtype == 8 =>
        val detailbase = detailRow.joke.get.base
        val detailsyst = detailRow.joke.get.syst
        NewsDetailsResponse(base.nid.get, base.docid, "", syst.ctime, base.pname, base.purl, syst.chid, None, None, None, Some(detailbase.content), incr.collect, incr.concern, commentnum, syst.icon, colFlag, conFlag, conPubFlag)

      case _ =>
        val detailbase = detailRow.news.get.base
        val detailsyst = detailRow.news.get.syst
        NewsDetailsResponse(base.nid.get, base.docid, base.title, syst.ctime, base.pname, base.purl, syst.chid, Some(detailsyst.inum), detailbase.tags, None, Some(detailbase.content), incr.collect, incr.concern, commentnum, syst.icon, colFlag, conFlag, conPubFlag)
    }
    newsDetailRow
  }
}

case class NewsFeedWithPublisherInfoResponse(info: Option[NewsPublisherRow] = None, news: Seq[NewsFeedResponse])

object NewsFeedWithPublisherInfoResponse {
  implicit val NewsFeedWithPublisherInfoResponseWrites: Writes[NewsFeedWithPublisherInfoResponse] = (
    (JsPath \ "info").writeNullable[NewsPublisherRow] ~
    (JsPath \ "news").write[Seq[NewsFeedResponse]]
  )(unlift(NewsFeedWithPublisherInfoResponse.unapply))

  def apply(news: Seq[NewsFeedResponse]): NewsFeedWithPublisherInfoResponse = new NewsFeedWithPublisherInfoResponse(None, news)
}

case class NewsFeedCache(nid: Long, style: Int, rtype: Option[Int] = None)

object NewsFeedCache {
  implicit val NewsFeedCacheWrites: Writes[NewsFeedCache] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "rtype").writeNullable[Int]
  )(unlift(NewsFeedCache.unapply))

  implicit val NewsFeedCacheReads: Reads[NewsFeedCache] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "rtype").readNullable[Int]
  )(NewsFeedCache.apply _)

  def from(newsFeedResponse: NewsFeedResponse): NewsFeedCache = {
    NewsFeedCache(newsFeedResponse.nid, newsFeedResponse.style, newsFeedResponse.rtype)
  }
}

case class ExtendData(nid: Long, clicktimes: Option[Int] = None)

object ExtendData {
  implicit val ExtendDataWrites: Writes[ExtendData] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "clicktimes").writeNullable[Int]
  )(unlift(ExtendData.unapply))

  implicit val ExtendDataReads: Reads[ExtendData] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "clicktimes").readNullable[Int]
  )(ExtendData.apply _)
}