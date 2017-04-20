package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.detail.DetailRow
import commons.models.joke.JokeDetailRow
import commons.models.news._
import commons.models.video.VideoDetailRow
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import dao.joke.JokeDetailDAO
import dao.news._
import dao.video.VideoDetailDAO
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[NewsService])
trait INewsService {
  //def findDetailsByNid(nid: Long): Future[Option[NewsDetailsResponse]]
  def refreshFeedByChannel(chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def loadFeedByChannel(chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def refreshFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def insert(newsRow: NewsRow): Future[Option[Long]]
  def delete(nid: Long): Future[Option[Long]]
  def updateCollect(nid: Long, collect: Int): Future[Option[Int]]
  def updateConcern(nid: Long, concern: Int): Future[Option[Int]]
  def updateComment(docid: String, comment: Int): Future[Option[Int]]
}

class NewsService @Inject() (val newsDetailDAO: NewsDetailDAO, val videoDetailDAO: VideoDetailDAO, val jokeDetailDAO: JokeDetailDAO, val newsDAO: NewsDAO, val newsRecommendDAO: NewsRecommendDAO, val newsPublisherDAO: NewsPublisherDAO) extends INewsService with NewsCacheService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  //  def findDetailsByNid(nid: Long): Future[Option[NewsDetailsResponse]] = {
  //    newsDetailDAO.findByNid(nid).map {
  //      case Some(news) => Some(NewsDetailsResponse.from(news._1, news._2))
  //      case _          => None
  //    }.recover {
  //      case NonFatal(e) =>
  //        Logger.error(s"Within NewsService.findDetailsByNid($nid): ${e.getMessage}")
  //        None
  //    }
  //  }

  //  def findDetailsWithProfileByNid(nid: Long, uidOpt: Option[Long], rtypeOpt: Option[Int]): Future[Option[NewsDetailsResponse]] = {
  //    val result: Future[Option[NewsDetailsResponse]] = uidOpt match {
  //      case None => newsDetailDAO.findByNid(nid).map {
  //        case Some(news) =>
  //          Some(NewsDetailsResponse.from(news._1, news._2))
  //        case _ => None
  //      }
  //      case Some(uid) => findDetailByNidWithProfile(nid, uid).map {
  //        case Some((newsfeed, newsdetail, c1, c2, c3)) => Some(NewsDetailsResponse.from(newsfeed, newsdetail, Some(c1), Some(c2), Some(c3)))
  //        case _                                        => None
  //      }
  //    }
  //    result.recover {
  //      case NonFatal(e) =>
  //        Logger.error(s"Within NewsService.findDetailsWithProfileByNid($nid, $uidOpt): ${e.getMessage}")
  //        None
  //    }
  //  }

  def findDetailsWithProfileByNid(nid: Long, uidOpt: Option[Long], rtypeOpt: Option[Int]): Future[Option[NewsDetailsResponse]] = {
    {
      //详情
      val detailResult = rtypeOpt match {
        //视频
        case Some(rtype) if rtype == 6 => videoDetailDAO.findDetailByNid(nid).map(DetailRow(None, _, None))
        //段子
        case Some(rtype) if rtype == 8 => jokeDetailDAO.findDetailByNid(nid).map(DetailRow(None, None, _))
        //新闻
        case Some(rtype)               => newsDetailDAO.findDetailByNid(nid).map(DetailRow(_, None, None))
        //未知, 三种同时取
        case _                         => allDetail(nid).map { detail => DetailRow(detail._1, detail._2, detail._3) }
      }

      //feed流信息和用户关注信息
      val feedResult = uidOpt match {
        case None =>
          newsDAO.findByNid(nid).map {
            case Some(newsfeed) => Some(newsfeed, None, None, None)
            case _              => None
          }

        case Some(uid) => newsDetailDAO.findByNidWithProfile(nid, uid).map {
          case Some((newsfeed, c1, c2, c3)) => Some(newsfeed, Some(c1), Some(c2), Some(c3))
          case _                            => None
        }
      }

      for {
        feedWithProfile <- feedResult
        detail <- detailResult
      } yield (Some(NewsDetailsResponse.from(feedWithProfile.get._1, detail, feedWithProfile.get._2, feedWithProfile.get._3, feedWithProfile.get._4)))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findDetailByNidWithProfile($nid, $uidOpt): ${e.getMessage}")
        None
    }
  }

  def allDetail(nid: Long): Future[(Option[NewsDetailRow], Option[VideoDetailRow], Option[JokeDetailRow])] = {
    {
      val newsdetail: Future[Option[NewsDetailRow]] = newsDetailDAO.findDetailByNid(nid)
      val videodetail: Future[Option[VideoDetailRow]] = videoDetailDAO.findDetailByNid(nid)
      val jokedetail: Future[Option[JokeDetailRow]] = jokeDetailDAO.findDetailByNid(nid)

      for {
        newsdetail <- newsdetail
        videodetail <- videodetail
        jokedetail <- jokedetail
      } yield (newsdetail, videodetail, jokedetail)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.allDetail($nid): ${e.getMessage}")
        (None, None, None)
    }
  }

  //  def findDetailByNidWithProfile(nid: Long, uid: Long): Future[Option[(NewsRow, NewsDetailRow, Int, Int, Int)]] = {
  //    {
  //      val feedWithProfile: Future[Option[(NewsRow, Int, Int, Int)]] = newsDetailDAO.findByNidWithProfile(nid, uid)
  //      //detail不清楚在哪个表中,不清楚是新闻,视频,段子,只能3个表同时取,最好取完放redis里
  //      val detail: Future[Option[NewsDetailRow]] = newsDetailDAO.findDetailByNid(nid)
  //      for {
  //        feedWithProfile <- feedWithProfile
  //        detail <- detail
  //      } yield (Some(feedWithProfile.get._1, detail.get, feedWithProfile.get._2, feedWithProfile.get._3, feedWithProfile.get._4))
  //    }.recover {
  //      case NonFatal(e) =>
  //        Logger.error(s"Within NewsService.findDetailByNidWithProfile($nid, $uid): ${e.getMessage}")
  //        None
  //    }
  //  }

  def findNextDetailsWithProfileByNid(nid: Long, uidOpt: Option[Long], chid: Long): Future[Option[NewsDetailsResponse]] = {
    val result: Future[Option[NewsDetailsResponse]] = uidOpt match {
      case None => newsDetailDAO.findNextByNid(nid, chid: Long).map {
        case Some(row) => Some(NewsDetailsResponse.from(row._1, DetailRow(Some(row._2))))
        case _         => None
      }
      case Some(uid) => newsDetailDAO.findNextByNid(nid, chid: Long).map {
        case Some(row) => Some(NewsDetailsResponse.from(row._1, DetailRow(Some(row._2))))
        case _         => None
      }
      //      case Some(uid) => findNextDetailByNidWithProfile(nid, uid, chid).map {
      //        case Some((newsfeed, newsdetail, c1, c2, c3)) => Some(NewsDetailsResponse.from(newsfeed, DetailRow(Some(newsdetail)), Some(c1), Some(c2), Some(c3)))
      //        case _                                        => None
      //      }
    }
    result.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findNextDetailsWithProfileByNid($nid, $uidOpt): ${e.getMessage}")
        None
    }
  }

  def findNextDetailByNidWithProfile(nid: Long, uid: Long, chid: Long): Future[Option[(NewsRow, NewsDetailRow, Int, Int, Int)]] = {
    {
      val feedWithProfile: Future[Option[(NewsRow, Int, Int, Int)]] = newsDetailDAO.findNextByNidWithProfile(nid: Long, uid: Long, chid: Long)
      val detail: Future[Option[NewsDetailRow]] = newsDetailDAO.findNextDetailByNid(nid: Long, chid: Long)
      for {
        feedWithProfile <- feedWithProfile
        detail <- detail
      } yield (Some(feedWithProfile.get._1, detail.get, feedWithProfile.get._2, feedWithProfile.get._3, feedWithProfile.get._4))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findNextDetailByNidWithProfile($nid, $uid): ${e.getMessage}")
        None
    }
  }

  def findLastDetailsWithProfileByNid(nid: Long, uidOpt: Option[Long], chid: Long): Future[Option[NewsDetailsResponse]] = {
    val result: Future[Option[NewsDetailsResponse]] = uidOpt match {
      case None => newsDetailDAO.findLastByNid(nid, chid: Long).map {
        case Some(row) => Some(NewsDetailsResponse.from(row._1, DetailRow(Some(row._2))))
        case _         => None
      }
      case Some(uid) => newsDetailDAO.findLastByNid(nid, chid: Long).map {
        case Some(row) => Some(NewsDetailsResponse.from(row._1, DetailRow(Some(row._2))))
        case _         => None
      }
      //      case Some(uid) => findLastDetailByNidWithProfile(nid, uid, chid).map {
      //        case Some((newsfeed, newsdetail, c1, c2, c3)) => Some(NewsDetailsResponse.from(newsfeed, DetailRow(Some(newsdetail)), Some(c1), Some(c2), Some(c3)))
      //        case _                                        => None
      //      }
    }
    result.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findLastDetailsWithProfileByNid($nid, $uidOpt): ${e.getMessage}")
        None
    }
  }

  def findLastDetailByNidWithProfile(nid: Long, uid: Long, chid: Long): Future[Option[(NewsRow, NewsDetailRow, Int, Int, Int)]] = {
    {
      val feedWithProfile: Future[Option[(NewsRow, Int, Int, Int)]] = newsDetailDAO.findLastByNidWithProfile(nid: Long, uid: Long, chid: Long)
      val detail: Future[Option[NewsDetailRow]] = newsDetailDAO.findLastDetailByNid(nid: Long, chid: Long)
      for {
        feedWithProfile <- feedWithProfile
        detail <- detail
      } yield (Some(feedWithProfile.get._1, detail.get, feedWithProfile.get._2, feedWithProfile.get._3, feedWithProfile.get._4))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findNextDetailByNidWithProfile($nid, $uid): ${e.getMessage}")
        None
    }
  }

  def loadFeedByRecommends(page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsDAO.loadByHot((page - 1) * count, count / 2, msecondsToDatetime(timeCursor), nid)
      val lostColdFO: Future[Seq[NewsRow]] = newsDAO.loadByCold((page - 1) * count, count / 2, msecondsToDatetime(timeCursor), nid)
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

  def refreshFeedByRecommends(page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
      val refreshHotFO: Future[Seq[NewsRow]] = newsDAO.refreshByHot((page - 1) * count, count / 2, newTimeCursor, nid)
      val refreshColdFO: Future[Seq[NewsRow]] = newsDAO.refreshByCold((page - 1) * count, count / 2, newTimeCursor, nid)
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

  def refreshFeedByChannel(chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

    val result = sechidOpt match {
      case Some(sechid) => newsDAO.refreshBySeChannel(chid, sechid, (page - 1) * count, count, newTimeCursor, nid)
      case None         => newsDAO.refreshByChannel(chid, (page - 1) * count, count, newTimeCursor, nid)
    }

    result.map { newsRows => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
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
        Logger.error(s"Within NewsService.loadFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    newsDAO.loadByLocation((page - 1) * count, count, msecondsToDatetime(timeCursor), province, city, district, nid).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByLocation($timeCursor, $province, $city, $district): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
    newsDAO.refreshByLocation((page - 1) * count, count, newTimeCursor, province, city, district, nid).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByLocation($timeCursor, $province, $city, $district): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedBySource(source: Long, page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    newsDAO.loadBySource(source, (page - 1) * count, count, msecondsToDatetime(timeCursor), nid).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedBySource($source, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedBySource(source: Long, page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
    newsDAO.refreshBySource(source, (page - 1) * count, count, newTimeCursor, nid).map {
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
    val nowTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(0))
    if (reqTimeCursor.isBefore(oldTimeCursor) || reqTimeCursor.isAfter(nowTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def insert(newsRow: NewsRow): Future[Option[Long]] = {
    //    val result: Future[Option[Long]] = newsRow.base.pname match {
    //      case Some(pname) => newsPublisherDAO.findByName(pname).flatMap {
    //        case Some(newsPublisherRow: NewsPublisherRow) => newsDAO.insert(newsRow.copy(base = newsRow.base.copy(icon = newsPublisherRow.icon))).map { nid => Some(nid) }
    //        case _ => newsDAO.insert(newsRow).map { nid => Some(nid) }
    //      }
    //      case _ => newsDAO.insert(newsRow).map { nid => Some(nid) }
    //    }
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
