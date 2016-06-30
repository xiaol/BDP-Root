package commons.models.news

import commons.models.community._
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

case class NewsRelatedResponse(
  search: Option[List[ASearch]],
  baike: Option[Baike],
  douban: Option[Douban],
  zhihu: List[Zhihu],
  weibo: List[Weibo],
  comment: List[WeiboComment])

object NewsRelatedResponse {
  implicit val NewsRelatedResponseWrites: Writes[NewsRelatedResponse] = (
    (JsPath \ "search").writeNullable[List[ASearch]] ~
    (JsPath \ "baike").writeNullable[Baike] ~
    (JsPath \ "douban").writeNullable[Douban] ~
    (JsPath \ "zhihu").write[List[Zhihu]] ~
    (JsPath \ "weibo").write[List[Weibo]] ~
    (JsPath \ "comment").write[List[WeiboComment]]
  )(unlift(NewsRelatedResponse.unapply))

  implicit val NewsRelatedResponseReads: Reads[NewsRelatedResponse] = (
    (JsPath \ "search").readNullable[List[ASearch]] ~
    (JsPath \ "baike").readNullable[Baike] ~
    (JsPath \ "douban").readNullable[Douban] ~
    (JsPath \ "zhihu").read[List[Zhihu]] ~
    (JsPath \ "weibo").read[List[Weibo]] ~
    (JsPath \ "comment").read[List[WeiboComment]]
  )(NewsRelatedResponse.apply _)
}
