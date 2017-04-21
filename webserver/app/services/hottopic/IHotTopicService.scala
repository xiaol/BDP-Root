package services.hottopic

import javax.inject.Inject

import commons.models.hottopic.HotNews
import dao.hottopic.HotNewsDAO
import play.api.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal

/**
 * Created by fengjigang on 17/4/20.
 */
trait IHotTopicService {
  def insert(hotnews: HotNews): Future[Long]
  def insertAll(hotNews: Seq[HotNews]): Future[Seq[Long]]
}

class HotTopicService @Inject() (val hotTopicDao: HotNewsDAO)(implicit ec: ExecutionContext) extends IHotTopicService {
  override def insert(hotnews: HotNews): Future[Long] = {
    hotTopicDao.insert(hotnews).recover {
      case NonFatal(e) =>
        Logger.error(s"Within IHotTopicService insert (HotNews): ${e.getMessage}")
        0
    }
  }

  override def insertAll(hotNews: Seq[HotNews]): Future[Seq[Long]] = {

    hotTopicDao.insertAll(hotNews).recover {
      case NonFatal(e) =>
        Logger.error(s"Within IHotTopicService insertAll (HotNews) :${e.getMessage}")
        Seq[Long](0)
    }
  }
}
