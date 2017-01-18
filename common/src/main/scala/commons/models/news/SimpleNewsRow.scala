package commons.models.news

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }

/**
 * Created by zhangshl on 17/1/12.
 */
case class SimpleNewsRow(nid: Long,
                         title: String,
                         imgs: Option[List[String]] = None)

object SimpleNewsRow {

  implicit val SimpleNewsRowWrites: Writes[SimpleNewsRow] = (
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "title").write[String] ~
    (JsPath \ "imgs").writeNullable[List[String]]
  )(unlift(SimpleNewsRow.unapply))

  implicit val SimpleNewsRowReads: Reads[SimpleNewsRow] = (
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "title").read[String] ~
    (JsPath \ "imgs").readNullable[List[String]]
  )(SimpleNewsRow.apply _)

}