package services.OtherCache

import commons.models.updateversion.UpdateVersion
import play.api.Logger
import play.api.libs.json.Json
import utils.RedisDriver.cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangshl on 17/4/24.
 */
trait OtherCacheService {
  //渠道版本号
  private val rowSuffix = "webapi:channel:version:"
  private val updateVersionName: ((Int, Int) => String) = (ctype: Int, ptype: Int) => s"$rowSuffix${ctype.toString}:${ptype.toString}"

  def getUpdateVersionCache(ctype: Int, ptype: Int): Future[Option[UpdateVersion]] = {
    cache.get[String](updateVersionName(ctype, ptype)).map {
      case Some(newsJson) => Some(Json.parse(newsJson).as[UpdateVersion])
      case _              => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within OtherCacheService.getUpdateVersionCache($ctype): ${e.getMessage}")
        None
    }
  }

  def setUpdateVersionCache(updateVersion: UpdateVersion): Future[Boolean] = {
    cache.set[String](updateVersionName(updateVersion.ctype, updateVersion.ptype), Json.toJson(updateVersion).toString, Some(60 * 60 * 24 * 7L)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within OtherCacheService.setUpdateVersionCache($updateVersion): ${e.getMessage}")
        false
    }
  }

}

object OtherCacheService extends OtherCacheService