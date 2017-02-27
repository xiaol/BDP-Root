package services.news

import commons.models.news.{ NewsFeedResponse, NewsRow }
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
  private val feedSuffix = "webapi:news:feed:uid:"
  private val newsRowName: (Long => String) = (nid: Long) => s"$rowSuffix${nid.toString}"
  private val newsFeedName: (Long => String) = (uid: Long) => s"$feedSuffix${uid.toString}"

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

  def getNewsFeedCache(uid: Long): Future[Option[Seq[NewsFeedResponse]]] = {
    cache.get[String](newsFeedName(uid)).map {
      case Some(newsJson) => Some(Json.parse(newsJson).as[Seq[NewsFeedResponse]])
      case _              => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.getNewsFeedCache($uid): ${e.getMessage}")
        None
    }
  }

  def setNewsFeedCache(uid: Long, newsfeed: Seq[NewsFeedResponse]): Future[Boolean] = {
    cache.set[String](newsFeedName(uid), Json.toJson(newsfeed).toString, Some(60 * 60 * 24 * 1L)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.setNewsFeedCache($uid, $newsfeed): ${e.getMessage}")
        false
    }
  }

  def remNewsFeedCache(uid: Long): Future[Long] = {
    cache.del(newsFeedName(uid)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.remNewsFeedCache($uid): ${e.getMessage}")
        0L
    }
  }

}

object NewsCacheService extends NewsCacheService