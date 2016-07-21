package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import commons.utils._
import dao.news.NewsPublisherDAO
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-07-18.
 *
 */

@ImplementedBy(classOf[NewsPublisherService])
trait INewsPublisherService {
  def insertOrDiscard(newsPublisherRow: NewsPublisherRow): Future[Either[DBExceptionMessage, Long]]
}

class NewsPublisherService @Inject() (val newsPublisherDAO: NewsPublisherDAO) extends INewsPublisherService {

  def insertOrDiscard(newsPublisherRow: NewsPublisherRow): Future[Either[DBExceptionMessage, Long]] = {
    newsPublisherDAO.insertOrDiscard(newsPublisherRow).map(Right(_)).recover {
      case e: PGDBException => Left(e.getErrorEntity)
      case NonFatal(e) =>
        Logger.error(s"Within NewsPublisherService.insertOrDiscard(${newsPublisherRow.toString}): ${e.getMessage}")
        Left(ExecutionFail(e.getMessage))
    }
  }
}