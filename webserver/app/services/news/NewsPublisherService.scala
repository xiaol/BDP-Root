package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import commons.utils._
import dao.news.NewsPublisherDAO
import play.api.Logger
import commons.utils.JodaUtils._

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

  def listNewsByPublisher(pname: String, page: Long, count: Long, tcursor: Long, infoFlag: Int): Future[Either[DBExceptionMessage, NewsFeedWithPublisherInfoResponse]] = {
    val result = infoFlag match {
      case 0 => newsPublisherDAO.listNewsByPublisher(pname, (page - 1) * count, count, msecondsToDatetime(tcursor)).map {
        case newsSeq: Seq[NewsRow] =>
          Right(NewsFeedWithPublisherInfoResponse(newsSeq.map(NewsFeedResponse.from)))
      }
      case _ => newsPublisherDAO.listNewsByPublisherWithPubInfo(pname, (page - 1) * count, count, msecondsToDatetime(tcursor)).map {
        case (publisher, newsSeq) =>
          Right(NewsFeedWithPublisherInfoResponse(Some(publisher), newsSeq.map(NewsFeedResponse.from)))
      }
    }
    result.recover {
      case e: PGDBException => Left(e.getErrorEntity)
      case NonFatal(e) =>
        Logger.error(s"Within NewsPublisherService.listNewsByPublisher(${pname.toString}): ${e.getMessage}")
        Left(ExecutionFail(e.getMessage))
    }
  }
}