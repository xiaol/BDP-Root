package commons.models.joke

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */
/*************************************段子详情表**************************************/
case class JokeDetailRowBase(nid: Long,
                             docid: String,
                             content: JsValue,
                             author: Option[String] = None,
                             avatar: Option[String] = None,
                             ptime: LocalDateTime,
                             pname: Option[String] = None)

object JokeDetailRowBase {
  implicit val JokeDetailRowBaseWrites: Writes[JokeDetailRowBase] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "docid").write[String] ~
    (JsPath \ "content").write[JsValue] ~
    (JsPath \ "author").writeNullable[String] ~
    (JsPath \ "avatar").writeNullable[String] ~
    (JsPath \ "ptime").write[LocalDateTime] ~
    (JsPath \ "pname").writeNullable[String]
  )(unlift(JokeDetailRowBase.unapply))

  implicit val JokeDetailRowBaseReads: Reads[JokeDetailRowBase] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "docid").read[String] ~
    (JsPath \ "content").read[JsValue] ~
    (JsPath \ "author").readNullable[String] ~
    (JsPath \ "avatar").readNullable[String] ~
    (JsPath \ "ptime").read[LocalDateTime] ~
    (JsPath \ "pname").readNullable[String]
  )(JokeDetailRowBase.apply _)
}

case class JokeDetailRowSyst(style: Option[Int] = None,
                             imgs: Option[List[String]] = None,
                             ctime: LocalDateTime,
                             chid: Long,
                             sechid: Option[Long] = None,
                             srid: Option[Long] = None,
                             icon: Option[String] = None)

object JokeDetailRowSyst {
  implicit val JokeDetailRowSystWrites: Writes[JokeDetailRowSyst] = (
    (JsPath \ "style").writeNullable[Int] ~
    (JsPath \ "imgs").writeNullable[List[String]] ~
    (JsPath \ "ctime").write[LocalDateTime] ~
    (JsPath \ "chid").write[Long] ~
    (JsPath \ "sechid").writeNullable[Long] ~
    (JsPath \ "srid").writeNullable[Long] ~
    (JsPath \ "icon").writeNullable[String]
  )(unlift(JokeDetailRowSyst.unapply))

  implicit val JokeDetailRowSystReads: Reads[JokeDetailRowSyst] = (
    (JsPath \ "style").readNullable[Int] ~
    (JsPath \ "imgs").readNullable[List[String]] ~
    (JsPath \ "ctime").read[LocalDateTime] ~
    (JsPath \ "chid").read[Long] ~
    (JsPath \ "sechid").readNullable[Long] ~
    (JsPath \ "srid").readNullable[Long] ~
    (JsPath \ "icon").readNullable[String]
  )(JokeDetailRowSyst.apply _)
}

case class JokeDetailRow(base: JokeDetailRowBase, syst: JokeDetailRowSyst)

object JokeDetailRow {
  implicit val JokeDetailRowWrites: Writes[JokeDetailRow] = (
    (JsPath \ "base").write[JokeDetailRowBase] ~
    (JsPath \ "syst").write[JokeDetailRowSyst]
  )(unlift(JokeDetailRow.unapply))

  implicit val JokeDetailRowReads: Reads[JokeDetailRow] = (
    (JsPath \ "base").read[JokeDetailRowBase] ~
    (JsPath \ "syst").read[JokeDetailRowSyst]
  )(JokeDetailRow.apply _)

}
