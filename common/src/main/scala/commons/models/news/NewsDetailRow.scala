package commons.models.news

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */
/*************************************新闻详情表**************************************/
case class NewsDetailRowBase(nid: Option[Long] = None,
                             url: String,
                             docid: String,
                             title: String,
                             content: JsValue,
                             author: Option[String] = None,
                             ptime: LocalDateTime,
                             pname: Option[String] = None,
                             purl: Option[String] = None,
                             tags: Option[List[String]] = None,
                             province: Option[String] = None,
                             city: Option[String] = None,
                             district: Option[String] = None)

object NewsDetailRowBase {
  implicit val NewsDetailRowBaseWrites: Writes[NewsDetailRowBase] = (
    (JsPath \ "nid").writeNullable[Long] ~
    (JsPath \ "url").write[String] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "content").write[JsValue] ~
    (JsPath \ "author").writeNullable[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String] ~
    (JsPath \ "purl").writeNullable[String] ~
    (JsPath \ "tags").writeNullable[List[String]] ~
    (JsPath \ "province").writeNullable[String] ~
    (JsPath \ "city").writeNullable[String] ~
    (JsPath \ "district").writeNullable[String]
  )(unlift(NewsDetailRowBase.unapply))

  implicit val NewsDetailRowBaseReads: Reads[NewsDetailRowBase] = (
    (JsPath \ "nid").readNullable[Long] ~
    (JsPath \ "url").read[String] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "content").read[JsValue] ~
    (JsPath \ "author").readNullable[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String] ~
    (JsPath \ "purl").readNullable[String] ~
    (JsPath \ "tags").readNullable[List[String]] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "district").readNullable[String]
  )(NewsDetailRowBase.apply _)
}

case class NewsDetailRowSyst(inum: Int,
                             style: Int,
                             imgs: Option[List[String]] = None,
                             ctime: LocalDateTime,
                             chid: Long,
                             sechid: Option[Long],
                             srid: Long,
                             icon: Option[String] = None)

object NewsDetailRowSyst {
  implicit val NewsDetailRowSystWrites: Writes[NewsDetailRowSyst] = (
    (JsPath \ "inum").write[Int] ~
    (JsPath \ "style").write[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "chid").write[Long] ~
    (JsPath \ "sechid").writeNullable[Long] ~
    (JsPath \ "srid").write[Long] ~
    (JsPath \ "icon").writeNullable[String]
  )(unlift(NewsDetailRowSyst.unapply))

  implicit val NewsDetailRowSystReads: Reads[NewsDetailRowSyst] = (
    (JsPath \ "inum").read[Int] ~
    (JsPath \ "style").read[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "chid").read[Long] ~
    (JsPath \ "sechid").readNullable[Long] ~
    (JsPath \ "srid").read[Long] ~
    (JsPath \ "icon").readNullable[String]
  )(NewsDetailRowSyst.apply _)
}

case class NewsDetailRow(base: NewsDetailRowBase, syst: NewsDetailRowSyst)

object NewsDetailRow {
  implicit val NewsDetailRowWrites: Writes[NewsDetailRow] = (
    (JsPath \ "base").write[NewsDetailRowBase] ~
    (JsPath \ "syst").write[NewsDetailRowSyst]
  )(unlift(NewsDetailRow.unapply))

  implicit val NewsDetailRowReads: Reads[NewsDetailRow] = (
    (JsPath \ "base").read[NewsDetailRowBase] ~
    (JsPath \ "syst").read[NewsDetailRowSyst]
  )(NewsDetailRow.apply _)

}
