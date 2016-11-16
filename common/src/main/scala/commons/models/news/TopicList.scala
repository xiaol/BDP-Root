package commons.models.news

import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }
import slick.jdbc.GetResult

/**
 * Created by zhangshl on 16/10/17.
 */

case class TopicList(id: Int,
                     name: String,
                     owner: Option[Int] = None,
                     cover: String,
                     description: String,
                     class_count: Int,
                     news_count: Int,
                     online: Int,
                     top: Int,
                     create_time: Option[LocalDateTime])

object TopicList {
  //  implicit val getTopicListResult = GetResult(r => TopicList(r.<<, r.<<))
  implicit val TopicListWrites: Writes[TopicList] = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "name").write[String] ~
    (JsPath \ "owner").writeNullable[Int] ~
    (JsPath \ "cover").write[String] ~
    (JsPath \ "description").write[String] ~
    (JsPath \ "class_count").write[Int] ~
    (JsPath \ "news_count").write[Int] ~
    (JsPath \ "online").write[Int] ~
    (JsPath \ "top").write[Int] ~
    (JsPath \ "create_time").writeNullable[LocalDateTime]
  )(unlift(TopicList.unapply))
}

case class TopicClassList(id: Int,
                          name: String,
                          topic: Int,
                          order: Option[Int] = None)

object TopicClassList {
  implicit val TopicClassListWrites: Writes[TopicClassList] = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "name").write[String] ~
    (JsPath \ "topic").write[Int] ~
    (JsPath \ "order").writeNullable[Int]
  )(unlift(TopicClassList.unapply))
}

case class TopicNews(id: Int,
                     topic: Int,
                     topic_class: Int,
                     news: Long,
                     user: Int,
                     create_time: Option[LocalDateTime],
                     order: Option[Int] = None)

object TopicNews {
  implicit val TopicNewsWrites: Writes[TopicNews] = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "topic").write[Int] ~
    (JsPath \ "topic_class").write[Int] ~
    (JsPath \ "news").write[Long] ~
    (JsPath \ "user").write[Int] ~
    (JsPath \ "create_time").writeNullable[LocalDateTime] ~
    (JsPath \ "order").writeNullable[Int]
  )(unlift(TopicNews.unapply))
}

//分类关联:分类 with 新闻
case class TopicClassRelationNews(topicClassList: TopicClassList, newsFeed: List[NewsFeedResponse])
object TopicClassRelationNews {
  implicit val TopicClassRelationNewsWrites: Writes[TopicClassRelationNews] = (
    (JsPath \ "topicClassBaseInfo").write[TopicClassList] ~
    (JsPath \ "newsFeed").write[List[NewsFeedResponse]]
  )(unlift(TopicClassRelationNews.unapply))
}

//topic关联:topic with 分类关联
case class TopicRelationClass(topicList: TopicList, topicClassList: List[TopicClassRelationNews])
object TopicRelationClass {
  implicit val TopicRelationClassWrites: Writes[TopicRelationClass] = (
    (JsPath \ "topicBaseInfo").write[TopicList] ~
    (JsPath \ "topicClass").write[List[TopicClassRelationNews]]
  )(unlift(TopicRelationClass.unapply))
}

case class TopicNewsRead(id: Option[Long] = None,
                         uid: Long,
                         nid: Long,
                         cid: Int,
                         tid: Int,
                         ctime: LocalDateTime)

object TopicNewsRead {
  implicit val TopicNewsReadWrites: Writes[TopicNewsRead] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "nid").write[Long] ~
    (JsPath \ "cid").write[Int] ~
    (JsPath \ "tid").write[Int] ~
    (JsPath \ "ctime").write[LocalDateTime]
  )(unlift(TopicNewsRead.unapply))

  implicit val TopicNewsReadReads: Reads[TopicNewsRead] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "nid").read[Long] ~
    (JsPath \ "cid").read[Int] ~
    (JsPath \ "tid").read[Int] ~
    (JsPath \ "ctime").read[LocalDateTime]
  )(TopicNewsRead.apply _)
}