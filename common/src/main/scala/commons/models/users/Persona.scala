package commons.models.users

import commons.models.news.NewsPublisherRow
import commons.models.userprofiles.{ AppInfo, UserProfilesInfo }
import commons.utils.Joda4PlayJsonImplicits._
import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }

/**
 * Created by zhangshl on 16/12/6.
 */
//用户画像
case class Persona(userProfilesInfo: Option[UserProfilesInfo],
                   appInfoList: Option[List[AppInfo]],
                   userTopicList: Option[List[UserTopic]],
                   newsPublisherRowList: Option[List[NewsPublisherRow]])

object Persona {
  implicit val PersonaWrites: Writes[Persona] = (
    (JsPath \ "userProfilesInfo").writeNullable[UserProfilesInfo] ~
    (JsPath \ "appInfoList").writeNullable[List[AppInfo]] ~
    (JsPath \ "userTopicList").writeNullable[List[UserTopic]] ~
    (JsPath \ "newsPublisherRowList").writeNullable[List[NewsPublisherRow]]
  )(unlift(Persona.unapply))

  implicit val PersonaReads: Reads[Persona] = (
    (JsPath \ "userProfilesInfo").readNullable[UserProfilesInfo] ~
    (JsPath \ "appInfoList").readNullable[List[AppInfo]] ~
    (JsPath \ "userTopicList").readNullable[List[UserTopic]] ~
    (JsPath \ "newsPublisherRowList").readNullable[List[NewsPublisherRow]]
  )(Persona.apply _)
}

//用户偏好话题
case class UserTopic(id: Option[Long] = None,
                     uid: Long,
                     begin_time: LocalDateTime,
                     last_time: LocalDateTime,
                     keywords: List[String],
                     failure_time: LocalDateTime,
                     ctime: LocalDateTime)

object UserTopic {
  implicit val UserTopicWrites: Writes[UserTopic] = (
    (JsPath \ "id").writeNullable[Long] ~
    (JsPath \ "uid").write[Long] ~
    (JsPath \ "begin_time").write[LocalDateTime] ~
    (JsPath \ "last_time").write[LocalDateTime] ~
    (JsPath \ "keywords").write[List[String]] ~
    (JsPath \ "failure_time").write[LocalDateTime] ~
    (JsPath \ "ctime").write[LocalDateTime]
  )(unlift(UserTopic.unapply))

  implicit val UserTopicReads: Reads[UserTopic] = (
    (JsPath \ "id").readNullable[Long] ~
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "begin_time").read[LocalDateTime] ~
    (JsPath \ "last_time").read[LocalDateTime] ~
    (JsPath \ "keywords").read[List[String]] ~
    (JsPath \ "failure_time").read[LocalDateTime] ~
    (JsPath \ "ctime").read[LocalDateTime]
  )(UserTopic.apply _)
}