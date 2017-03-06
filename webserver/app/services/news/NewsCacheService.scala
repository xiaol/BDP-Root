package services.news

import akka.util.ByteString
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
  private val stateSuffix = "webapi:news:state:uid:"
  private val commonfeedSuffix = "webapi:news:feed:common"
  private val newsRowName: (Long => String) = (nid: Long) => s"$rowSuffix${nid.toString}"
  private val newsFeedName: (Long => String) = (uid: Long) => s"$feedSuffix${uid.toString}"
  private val userstateName: (Long => String) = (uid: Long) => s"$stateSuffix${uid.toString}"

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
    cache.set[String](newsRowName(newsRow.base.nid.get), Json.toJson(newsRow).toString, Some(60 * 60 * 24 * 3L)).recover {
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
    cache.set[String](newsFeedName(uid), Json.toJson(newsfeed).toString, Some(60 * 60 * 1L)).recover {
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

  def setNewsFeedCommonSetCache(newsfeed: Seq[String]): Future[Long] = {
    cache.sadd(commonfeedSuffix, newsfeed: _*).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.setNewsFeedCommonSetCache($newsfeed): ${e.getMessage}")
        0L
    }
  }

  def getNewsFeedCommonSetCache(limit: Long): Future[Option[Seq[NewsFeedResponse]]] = {
    cache.srandmember(commonfeedSuffix, limit).map {
      case news: Seq[ByteString] if news.nonEmpty =>
        val seq = news.map { newsRow => Json.parse(newsRow.utf8String).as[NewsFeedResponse] }
        Some(seq)
      case _ => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.getNewsFeedCommonSetCache(): ${e.getMessage}")
        None
    }
  }

  def remNewsFeedCommonSetCache(): Future[Long] = {
    cache.del(commonfeedSuffix).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.remNewsFeedCommonSetCache(): ${e.getMessage}")
        0L
    }
  }

  def getUserStateCache(uid: Long): Future[Option[String]] = {
    cache.get[String](userstateName(uid)).map {
      case Some(state) => Some(state)
      case _           => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.getUserStateCache($uid): ${e.getMessage}")
        None
    }
  }

  def setUserStateCache(uid: Long, state: String): Future[Boolean] = {
    cache.set[String](userstateName(uid), state, Some(60 * 60L)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.setUserStateCache($uid, $state): ${e.getMessage}")
        false
    }
  }

  def remUserStateCache(uid: Long): Future[Long] = {
    cache.del(userstateName(uid)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.remUserStateCache($uid): ${e.getMessage}")
        0L
    }
  }

}

object NewsCacheService extends NewsCacheService