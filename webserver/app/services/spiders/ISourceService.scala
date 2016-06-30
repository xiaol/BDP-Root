package services.spiders

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.spiders.{ SourceResponse, SourceRow }
import dao.spiders.SourceDAO
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-12.
 *
 */

@ImplementedBy(classOf[SourceService])
trait ISourceService {
  def list(page: Long, count: Long): Future[Seq[SourceRow]]
  def listAll(): Future[Seq[SourceRow]]
  def listByState(state: Int = 1, status: Int = 1, page: Long, count: Long): Future[Seq[SourceRow]]
  def listByOnline(state: Int, status: Int, page: Long, count: Long): Future[Seq[SourceResponse]]
  def listByQueue(queue: String, page: Long, count: Long): Future[Seq[SourceRow]]
  def findById(id: Long): Future[Option[SourceRow]]
  def update(id: Long, sourceRow: SourceRow): Future[Option[SourceRow]]
  def insert(sourceRow: SourceRow): Future[Option[Long]]
  def delete(id: Long): Future[Option[Long]]
  def count(): Future[Option[Int]]
}

class SourceService @Inject() (val sourceDAO: SourceDAO) extends ISourceService {

  def list(page: Long, count: Long): Future[Seq[SourceRow]] = {
    sourceDAO.list((page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.list($page, $count): ${e.getMessage}")
        Seq[SourceRow]()
    }
  }

  def listAll(): Future[Seq[SourceRow]] = {
    sourceDAO.listAll().recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.listAll(): ${e.getMessage}")
        Seq[SourceRow]()
    }
  }

  def listByState(state: Int = 1, status: Int = 1, page: Long, count: Long): Future[Seq[SourceRow]] = {
    sourceDAO.listByStateAndStatus(state, status, (page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.listByState($state, $status, $page, $count): ${e.getMessage}")
        Seq[SourceRow]()
    }
  }

  // TODO: state=1 && status=1
  def listByOnline(state: Int = 0, status: Int = 1, page: Long, count: Long): Future[Seq[SourceResponse]] = {
    sourceDAO.listByStateAndStatus(state, status, (page - 1) * count, count).map { case s => s.map(SourceResponse.from) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.listByOnline($status, $status, $page, $count): ${e.getMessage}")
        Seq[SourceResponse]()
    }
  }

  def listByQueue(queue: String, page: Long, count: Long): Future[Seq[SourceRow]] = {
    sourceDAO.listByQueue(queue, (page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.listByQueue($queue, $page, $count): ${e.getMessage}")
        Seq[SourceRow]()
    }
  }

  def findById(id: Long): Future[Option[SourceRow]] = {
    sourceDAO.findById(id).recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.findById($id): ${e.getMessage}")
        None
    }
  }

  def update(id: Long, sourceRow: SourceRow): Future[Option[SourceRow]] = {
    sourceDAO.update(id, sourceRow).recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.update($id, ${sourceRow.toString}): ${e.getMessage}")
        None
    }
  }

  def insert(sourceRow: SourceRow): Future[Option[Long]] = {
    sourceDAO.insert(sourceRow).map { id => Some(id) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.insert(${sourceRow.toString}): ${e.getMessage}")
        None
    }
  }

  def delete(id: Long): Future[Option[Long]] = {
    sourceDAO.delete(id).recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.delete($id): ${e.getMessage}")
        None
    }
  }

  def count(): Future[Option[Int]] = {
    sourceDAO.count().map { c => Some(c) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within SourceService.count(): ${e.getMessage}")
        None
    }
  }
}