package controllers

import javax.inject.Inject

import commons.models.pvuv.PvUvofDay
import play.api.mvc.{ Action, Controller }
import services.pvuvcount.PvuvCountService
import utils.Response._

import scala.concurrent.ExecutionContext

class PvUvController @Inject() (val PvuvCountService: PvuvCountService)(implicit ec: ExecutionContext)
    extends Controller {

  def pvuv(page: Long, count: Long) = Action.async { implicit request =>
    PvuvCountService.pvuvCount(page, count).map {
      case pvuvs: Seq[PvUvofDay] if pvuvs.nonEmpty => ServerSucced(pvuvs)
      case _                                       => DataEmptyError(s"$page, $count,")
    }
  }
}