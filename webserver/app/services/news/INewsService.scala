package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import dao.news.NewsDAO
import commons.models.news._
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import org.joda.time.LocalDateTime
import play.api.Logger
import scala.util.Random

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[NewsService])
trait INewsService {
  def findDetailsByNid(nid: Long): Future[Option[NewsDetailsResponse]]
  def loadFeedByChannel(channel: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]]
  def refreshFeedByChannel(channel: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]]
  def loadFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String]): Future[Seq[NewsFeedResponse]]
  def refreshFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String]): Future[Seq[NewsFeedResponse]]
  def insert(newsRow: NewsRow): Future[Option[Long]]
  def delete(nid: Long): Future[Option[Long]]
  def updateCollect(nid: Long, collect: Int): Future[Option[Int]]
  def updateConcern(nid: Long, concern: Int): Future[Option[Int]]
  def updateComment(docid: String, comment: Int): Future[Option[Int]]
}

class NewsService @Inject() (val newsDAO: NewsDAO) extends INewsService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def findDetailsByNid(nid: Long): Future[Option[NewsDetailsResponse]] = {
    newsDAO.findByNid(nid).map {
      case Some(newsRow) => Some(NewsDetailsResponse.from(newsRow))
      case _             => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findDetailsByUrl($nid): ${e.getMessage}")
        None
    }
  }

  def loadFeedByRecommends(page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsDAO.loadByHot((page - 1) * count, count / 2, msecondsToDatetime(timeCursor))
      val lostColdFO: Future[Seq[NewsRow]] = newsDAO.loadByCold((page - 1) * count, count / 2, msecondsToDatetime(timeCursor))
      for {
        hots <- loadHotFO
        colds <- lostColdFO
      } yield {
        hots ++: colds match {
          case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByRecommends($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByRecommends(page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
      val refreshHotFO: Future[Seq[NewsRow]] = newsDAO.refreshByHot((page - 1) * count, count / 2, newTimeCursor)
      val refreshColdFO: Future[Seq[NewsRow]] = newsDAO.refreshByCold((page - 1) * count, count / 2, newTimeCursor)
      for {
        hots <- refreshHotFO
        colds <- refreshColdFO
      } yield {
        hots ++: colds match {
          case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByRecommends($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByChannel(channel: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    newsDAO.loadByChannel(channel, (page - 1) * count, count, msecondsToDatetime(timeCursor)).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByChannel($channel, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByChannel(channel: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
    newsDAO.refreshByChannel(channel, (page - 1) * count, count, newTimeCursor).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByChannel($channel, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String]): Future[Seq[NewsFeedResponse]] = {
    newsDAO.loadByLocation((page - 1) * count, count, msecondsToDatetime(timeCursor), province, city, district).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByLocation($timeCursor, $province, $city, $district): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
    newsDAO.refreshByLocation((page - 1) * count, count, newTimeCursor, province, city, district).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByLocation($timeCursor, $province, $city, $district): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedBySource(source: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    newsDAO.loadBySource(source, (page - 1) * count, count, msecondsToDatetime(timeCursor)).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedBySource($source, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedBySource(source: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
    newsDAO.refreshBySource(source, (page - 1) * count, count, newTimeCursor).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedBySource($source, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    if (reqTimeCursor.isBefore(oldTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def insert(newsRow: NewsRow): Future[Option[Long]] = {
    newsDAO.insert(newsRow).map { nid => Some(nid) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.insert(${newsRow.base.url}, ${newsRow.base.docid}, ${newsRow.base.title}): ${e.getMessage}")
        None
    }
  }

  def delete(nid: Long): Future[Option[Long]] = {
    newsDAO.delete(nid).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.delete($nid): ${e.getMessage}")
        None
    }
  }

  def updateCollect(nid: Long, collect: Int): Future[Option[Int]] = {
    newsDAO.findByNid(nid).flatMap {
      case Some(nr) =>
        val incr = nr.incr
        newsDAO.update(nid, nr.copy(incr = incr.copy(collect = incr.collect + collect))).map(_ => Some(incr.collect + collect))
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.updateCollect($nid, $collect): ${e.getMessage}")
        None
    }
  }

  def updateConcern(nid: Long, concern: Int): Future[Option[Int]] = {
    newsDAO.findByNid(nid).flatMap {
      case Some(nr) =>
        val incr = nr.incr
        newsDAO.update(nid, nr.copy(incr = incr.copy(concern = incr.concern + concern))).map(_ => Some(incr.concern + concern))
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.updateConcern($nid, $concern): ${e.getMessage}")
        None
    }
  }

  def updateComment(docid: String, comment: Int): Future[Option[Int]] = {
    newsDAO.findByDocid(docid).flatMap {
      case Some(nr) =>
        val incr = nr.incr
        newsDAO.update(nr.base.nid.get, nr.copy(incr = incr.copy(comment = incr.comment + comment))).map(_ => Some(incr.comment + comment))
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.updateComment($docid, $comment): ${e.getMessage}")
        None
    }
  }
}