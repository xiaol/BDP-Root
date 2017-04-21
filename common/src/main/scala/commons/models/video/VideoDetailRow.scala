package commons.models.video

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */
/*************************************视频详情表**************************************/
case class VideoDetailRowBase(nid: Long,
                              url: String,
                              docid: String,
                              title: String,
                              content: Option[JsValue] = None,
                              author: Option[String] = None,
                              ptime: LocalDateTime,
                              pname: Option[String] = None,
                              tags: Option[List[String]] = None)

object VideoDetailRowBase {
  implicit val VideoDetailRowBaseWrites: Writes[VideoDetailRowBase] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "url").write[String] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "content").writeNullable[JsValue] ~
    (JsPath \ "author").writeNullable[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "tags").writeNullable[List[String]]
  )(unlift(VideoDetailRowBase.unapply))

  implicit val VideoDetailRowBaseReads: Reads[VideoDetailRowBase] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "url").read[String] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "content").readNullable[JsValue] ~
    (JsPath \ "author").readNullable[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "tags").readNullable[List[String]]
  )(VideoDetailRowBase.apply _)
}

case class VideoDetailRowSyst(style: Int,
                              imgs: Option[List[String]] = None,
                              ctime: LocalDateTime,
                              chid: Long,
                              sechid: Option[Long],
                              srid: Option[Long],
                              icon: Option[String] = None,
                              videourl: Option[String] = None,
                              duration: Option[Int] = None)

object VideoDetailRowSyst {
  implicit val VideoDetailRowSystWrites: Writes[VideoDetailRowSyst] = (
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "chid").write[Long] ~
    (JsPath \ "sechid").writeNullable[Long] ~
    (JsPath \ "srid").writeNullable[Long] ~
    (JsPath \ "icon").writeNullable[String] ~
    (JsPath \ "videourl").writeNullable[String] ~
    (JsPath \ "duration").writeNullable[Int]
  )(unlift(VideoDetailRowSyst.unapply))

  implicit val VideoDetailRowSystReads: Reads[VideoDetailRowSyst] = (
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "chid").read[Long] ~
    (JsPath \ "sechid").readNullable[Long] ~
    (JsPath \ "srid").readNullable[Long] ~
    (JsPath \ "icon").readNullable[String] ~
    (JsPath \ "videourl").readNullable[String] ~
    (JsPath \ "duration").readNullable[Int]
  )(VideoDetailRowSyst.apply _)
}

case class VideoDetailRow(base: VideoDetailRowBase, syst: VideoDetailRowSyst)

object VideoDetailRow {
  implicit val VideoDetailRowWrites: Writes[VideoDetailRow] = (
    (JsPath \ "base").write[VideoDetailRowBase] ~
    (JsPath \ "syst").write[VideoDetailRowSyst]
  )(unlift(VideoDetailRow.unapply))

  implicit val VideoDetailRowReads: Reads[VideoDetailRow] = (
    (JsPath \ "base").read[VideoDetailRowBase] ~
    (JsPath \ "syst").read[VideoDetailRowSyst]
  )(VideoDetailRow.apply _)

}
