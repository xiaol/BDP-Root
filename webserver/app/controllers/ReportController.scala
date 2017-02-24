package controllers

import javax.inject.Inject

import commons.models.report.TopClickNews
import play.api.mvc.{ Action, Controller }
import services.report.NewsReportService
import utils.Response._

import scala.concurrent.ExecutionContext

/**
 * Created by zhangshl on 17/2/23.
 */
class ReportController @Inject() (val newsReportService: NewsReportService)(implicit ec: ExecutionContext)
    extends Controller {

  def topClickNews(ctype: Int, ptype: Int, page: Long, count: Long) = Action.async { implicit request =>
    newsReportService.topClickNews(ctype: Int, ptype: Int, page: Long, count: Long).map {
      case top: Seq[TopClickNews] if top.nonEmpty => ServerSucced(top)
      case _                                      => DataEmptyError(s"$ctype, $ptype, $page, $count")
    }
  }
}