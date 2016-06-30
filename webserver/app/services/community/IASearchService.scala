package services.community

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.community.ASearchRow
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import dao.community.ASearchDAO

/**
 * Created by zhange on 2016-05-21.
 *
 */

@ImplementedBy(classOf[ASearchService])
trait IASearchService {
  def listByRefer(refer: String, page: Long, count: Long): Future[Seq[ASearchRow]]
  def insertMulti(searchItemRows: Seq[ASearchRow]): Future[Seq[Long]]
}

class ASearchService @Inject() (val asearchDAO: ASearchDAO) extends IASearchService {

  def listByRefer(refer: String, page: Long, count: Long): Future[Seq[ASearchRow]] = {
    asearchDAO.listByRefer(refer, (page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within ASearchService.listByRefer($refer, $page, $count): ${e.getMessage}")
        Seq[ASearchRow]()
    }
  }

  def insertMulti(searchItemRows: Seq[ASearchRow]): Future[Seq[Long]] = {
    asearchDAO.insertAll(searchItemRows).recover {
      case NonFatal(e) =>
        Logger.error(s"Within ASearchService.insertMulti(row.size: ${searchItemRows.size}, refer: ${searchItemRows.map(_.refer).toSet}): ${e.getMessage}")
        Seq[Long]()
    }
  }
}
