package services.users

import commons.models.users.UserRow
import play.api.Logger
import play.api.libs.json.Json
import utils.RedisDriver.cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-04-27.
 *
 */

trait UserCacheService {

  private val rowSuffix = ":urow"
  private val userRowName: (Long => String) = (uid: Long) => s"${uid.toString}$rowSuffix"

  def getUserRowCache(uid: Long): Future[Option[UserRow]] = {
    cache.get[String](userRowName(uid)).map {
      case Some(userJson) => Some(Json.parse(userJson).as[UserRow])
      case _              => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserCacheService.getUserRowCache($uid): ${e.getMessage}")
        None
    }
  }

  def setUserRowCache(userRow: UserRow): Future[Boolean] = {
    cache.set[String](userRowName(userRow.sys.uid.get), Json.toJson(userRow).toString, Some(60 * 60 * 24 * 7L)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserCacheService.setUserRowCache($userRow): ${e.getMessage}")
        false
    }
  }

  def remUserRowCache(uid: Long): Future[Long] = {
    cache.del(userRowName(uid)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within UserCacheService.remUserRowCache($uid): ${e.getMessage}")
        0L
    }
  }
}

object UserCacheService extends UserCacheService