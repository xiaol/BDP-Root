package services.spiders

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.spiders.QueueRow
import dao.spiders.QueueDAO
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-12.
 *
 */

@ImplementedBy(classOf[QueueService])
trait IQueueService {
  def list(page: Long, count: Long): Future[Seq[QueueRow]]
  def findByQueue(queue: String): Future[Option[QueueRow]]
  def listBySpider(spider: String, offset: Long, limit: Long): Future[Seq[QueueRow]]
  def insert(queueRow: QueueRow): Future[Option[String]]
  def update(queue: String, queueRow: QueueRow): Future[Option[QueueRow]]
  def delete(queue: String): Future[Option[String]]
  def count(): Future[Option[Int]]
}

class QueueService @Inject() (val queueDAO: QueueDAO) extends IQueueService {

  def list(page: Long, count: Long): Future[Seq[QueueRow]] = {
    queueDAO.list((page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within QueueService.list($page, $count): ${e.getMessage}")
        Seq[QueueRow]()
    }
  }

  def findByQueue(queue: String): Future[Option[QueueRow]] = {
    queueDAO.findByQueue(queue).recover {
      case NonFatal(e) =>
        Logger.error(s"Within QueueService.findByQueue($queue): ${e.getMessage}")
        None
    }
  }

  def listBySpider(spider: String, page: Long, count: Long): Future[Seq[QueueRow]] = {
    queueDAO.listBySpider(spider, (page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within QueueService.listBySpider($spider, $page, $count): ${e.getMessage}")
        Seq[QueueRow]()
    }
  }

  def insert(queueRow: QueueRow): Future[Option[String]] = {
    queueDAO.insert(queueRow).map { q => Some(q) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QueueService.insert(${queueRow.toString}): ${e.getMessage}")
        None
    }
  }

  def update(queue: String, queueRow: QueueRow): Future[Option[QueueRow]] = {
    queueDAO.update(queue, queueRow).recover {
      case NonFatal(e) =>
        Logger.error(s"Within QueueService.update($queue, ${queueRow.toString}): ${e.getMessage}")
        None
    }
  }

  def delete(queue: String): Future[Option[String]] = {
    queueDAO.delete(queue).recover {
      case NonFatal(e) =>
        Logger.error(s"Within QueueService.delete($queue): ${e.getMessage}")
        None
    }
  }

  def count(): Future[Option[Int]] = {
    queueDAO.count().map { c => Some(c) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QueueService.count(): ${e.getMessage}")
        None
    }
  }
}