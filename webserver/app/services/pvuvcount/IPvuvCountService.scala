package services.pvuvcount

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.pvuv.PvUvofDay
import commons.models.spiders.SourceResponse
import dao.pvuvcount.PvuvCountDAO
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@ImplementedBy(classOf[PvuvCountService])
trait IPvuvCountService {
  def pvuvCount(page: Long, count: Long): Future[Seq[PvUvofDay]]
}

class PvuvCountService @Inject() (val PvuvCountDAO: PvuvCountDAO) extends IPvuvCountService {

  def pvuvCount(page: Long, count: Long): Future[Seq[PvUvofDay]] = {
    PvuvCountDAO.pvuvCount((page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within PvuvCountService.pvuvCount($page, $count): ${e.getMessage}")
        Seq[PvUvofDay]()
    }
  }
}