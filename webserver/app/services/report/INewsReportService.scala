package services.report

import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.report.TopClickNews
import dao.report.NewsReportDao
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@ImplementedBy(classOf[NewsReportService])
trait INewsReportService {
  def topClickNews(ctype: Int, ptype: Int, page: Long, count: Long): Future[Seq[TopClickNews]]
}

class NewsReportService @Inject() (val newsReportDao: NewsReportDao) extends INewsReportService {

  def topClickNews(ctype: Int, ptype: Int, page: Long, count: Long): Future[Seq[TopClickNews]] = {
    newsReportDao.topClickNews(ctype: Int, ptype: Int, (page - 1) * count, count).map { seq =>
      seq.map { top => TopClickNews(top._1, top._2, top._3, top._4, top._5, top._6, LocalDateTime.fromDateFields(new Date(top._7.getTime))) }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsReportService.topClickNews($ctype, $ptype, $page, $count): ${e.getMessage}")
        Seq[TopClickNews]()
    }
  }

}