package services.hottopic

import play.api.Logger
import utils.RedisDriver.cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by fengjigang on 17/4/21.
 */

trait IHotWordsCacheService {
  val key_hotwords = "webapi:news:hotwords"
  def getHotWordsCache(): Future[Option[String]]
  def setHotWordsCache(words: String): Future[Boolean]
}
class HotWordsCacheService extends IHotWordsCacheService {
  override def getHotWordsCache(): Future[Option[String]] = {
    cache.get[String](key_hotwords).recover {
      case NonFatal(e) =>
        Logger.error(s"Within HotWordsCacheService.getHotWordsCache: ${e.getMessage}")
        None
    }
  }

  override def setHotWordsCache(words: String): Future[Boolean] = {
    cache.set[String](key_hotwords, words).recover {
      case NonFatal(e) =>
        Logger.error(s"Within HotWordsCacheService.setHotWordsCache: ${e.getMessage}")
        false
    }
  }
}
