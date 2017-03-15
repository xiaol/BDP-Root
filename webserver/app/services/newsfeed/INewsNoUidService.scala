package services.newsfeed

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import dao.news._
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[NewsNoUidService])
trait INewsNoUidService {
  //没有用户id,刷奇点频道
  def refreshQidian(page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def loadQidian(page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]

  //没有用户id,刷其他频道
  def refreshFeedByChannel(chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def loadFeedByChannel(chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]
}

class NewsNoUidService @Inject() (val newsDAO: NewsDAO) extends INewsNoUidService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def refreshQidian(page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
      val refreshHotFO: Future[Seq[NewsRow]] = newsDAO.refreshByCommon((page - 1) * count, count / 2, newTimeCursor, nid)
      for {
        hots <- refreshHotFO
      } yield {
        hots match {
          case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsNoUidService.refreshQidian($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadQidian(page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsDAO.loadByCommon((page - 1) * count, count / 2, msecondsToDatetime(timeCursor), nid)
      for {
        hots <- loadHotFO
      } yield {
        hots match {
          case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsNoUidService.loadQidian($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByChannel(chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

    val result = sechidOpt match {
      case Some(sechid) => newsDAO.refreshBySeChannel(chid, sechid, (page - 1) * count, count, newTimeCursor, nid)
      case None         => newsDAO.refreshByChannel(chid, (page - 1) * count, count, newTimeCursor, nid)
    }

    result.map { newsRows => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsNoUidService.refreshFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByChannel(chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

    val result = sechidOpt match {
      case Some(sechid) => newsDAO.loadBySeChannel(chid, sechid, (page - 1) * count, count, newTimeCursor, nid)
      case None         => newsDAO.loadByChannel(chid, (page - 1) * count, count, newTimeCursor, nid)
    }

    result.map { newsRows => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsNoUidService.loadFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    val nowTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(0))
    if (reqTimeCursor.isBefore(oldTimeCursor) || reqTimeCursor.isAfter(nowTimeCursor)) oldTimeCursor else reqTimeCursor
  }
}
