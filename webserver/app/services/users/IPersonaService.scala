package services.users

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.NewsPublisherRow
import commons.models.userprofiles.{ AppInfo, UserProfilesInfo }
import commons.models.users._
import dao.userprofiles.ConcernPublisherDAO
import dao.users.{ AppInfoDAO, UserProfilesInfoDAO, UserTopicDao }
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangsl on 2016-12-01.
 *
 */

@ImplementedBy(classOf[PersonaService])
trait IPersonaService {
  def getPersonaByUid(uid: Long): Future[Option[Persona]]
}

class PersonaService @Inject() (val appInfoDAO: AppInfoDAO, val userProfilesInfoDAO: UserProfilesInfoDAO, val concernPublisherDAO: ConcernPublisherDAO, val userTopicDao: UserTopicDao) extends IPersonaService {

  def getPersonaByUid(uid: Long): Future[Option[Persona]] = {
    {
      val concernPublisherFO: Future[Seq[NewsPublisherRow]] = concernPublisherDAO.listNewsPublisherByConcerns(uid, 0, 1000)
      val userProfilesInfoFO: Future[Option[UserProfilesInfo]] = userProfilesInfoDAO.findByUid(uid)
      val appInfoFO: Future[Seq[AppInfo]] = appInfoDAO.findByUid(uid)
      val userTopicsFO: Future[Seq[UserTopic]] = userTopicDao.findByUid(uid)

      val r: Future[Option[Persona]] = for {
        concernPublisher <- concernPublisherFO
        userProfilesInfo <- userProfilesInfoFO
        appInfo <- appInfoFO
        userTopics <- userTopicsFO
      } yield {
        Some(Persona(userProfilesInfo, Some(appInfo.toList), Some(userTopics.toList), Some(concernPublisher.toList)))
      }
      r
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within PersonaService.getPersonaByUid($uid): ${e.getMessage}")
        None
    }
  }

}
