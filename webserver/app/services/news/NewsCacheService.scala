package services.news

import commons.models.news.NewsRow
import play.api.Logger
import play.api.libs.json.Json
import utils.RedisDriver.cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangshl on 17/2/10.
 */
trait NewsCacheService {
  private val rowSuffix = "webapi:news:id:"
  private val newsRowName: (Long => String) = (nid: Long) => s"$rowSuffix${nid.toString}"

  def getNewsRowCache(nid: Long): Future[Option[NewsRow]] = {
    cache.get[String](newsRowName(nid)).map {
      case Some(newsJson) => Some(Json.parse(newsJson).as[NewsRow])
      case _              => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.getNewsRowCache($nid): ${e.getMessage}")
        None
    }
  }

  def setNewsRowCache(newsRow: NewsRow): Future[Boolean] = {
    cache.set[String](newsRowName(newsRow.base.nid.get), Json.toJson(newsRow).toString, Some(60 * 60 * 24 * 7L)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.setNewsRowCache($newsRow): ${e.getMessage}")
        false
    }
  }

  def remNewsRowCache(nid: Long): Future[Long] = {
    cache.del(newsRowName(nid)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.remNewsRowCache($nid): ${e.getMessage}")
        0L
    }
  }
}

object NewsCacheService extends NewsCacheService