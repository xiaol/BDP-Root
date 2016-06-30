package commons.models.userprofiles

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-24.
 *
 */

case class UserChannels(channels: List[String])

object UserChannels {

  implicit val UserChannelFormat: Format[UserChannels] =
    (__ \ "channels").format[List[String]].inmap(chs => UserChannels(chs), (userChannels: UserChannels) => userChannels.channels)
}
