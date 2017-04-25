package commons.models.updateversion

import commons.utils.Joda4PlayJsonImplicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhangshl on 2017/4/24.
 */

case class UpdateVersion(ctype: Int,
                         ptype: Int,
                         version: String,
                         version_code: Int,
                         updateLog: Option[String] = None,
                         downloadLink: String,
                         forceUpdate: Boolean,
                         md5: String)

object UpdateVersion {
  implicit val UpdateVersionRowWrites: Writes[UpdateVersion] = (
    (JsPath \ "ctype").write[Int] ~
    (JsPath \ "ptype").write[Int] ~
    (JsPath \ "version").write[String] ~
    (JsPath \ "version_code").write[Int] ~
    (JsPath \ "updateLog").writeNullable[String] ~
    (JsPath \ "downloadLink").write[String] ~
    (JsPath \ "forceUpdate").write[Boolean] ~
    (JsPath \ "md5").write[String]
  )(unlift(UpdateVersion.unapply))

  implicit val UpdateVersionRowReads: Reads[UpdateVersion] = (
    (JsPath \ "ctype").read[Int] ~
    (JsPath \ "ptype").read[Int] ~
    (JsPath \ "version").read[String] ~
    (JsPath \ "version_code").read[Int] ~
    (JsPath \ "updateLog").readNullable[String] ~
    (JsPath \ "downloadLink").read[String] ~
    (JsPath \ "forceUpdate").read[Boolean] ~
    (JsPath \ "md5").read[String]
  )(UpdateVersion.apply _)
}
