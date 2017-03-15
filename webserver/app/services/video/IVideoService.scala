package services.video

import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import commons.models.video.VideoRow
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import dao.news._
import dao.newsfeed.NewsFeedDao
import dao.video.VideoDAO
import org.joda.time.LocalDateTime
import play.api.Logger
import services.advertisement.AdResponseService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random
import scala.util.control.NonFatal

/**
 * Created by zhangshl on 16/7/15.
 */
@ImplementedBy(classOf[VideoService])
trait IVideoService {
  def refreshFeedWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def loadFeedWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]]
}

class VideoService @Inject() (val videoDAO: VideoDAO, val newsResponseDao: NewsResponseDao, val adResponseService: AdResponseService, val newsFeedDao: NewsFeedDao, val newsRecommendReadDAO: NewsRecommendReadDAO) extends IVideoService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def refreshFeedWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      val result = newsResponseDao.video((page - 1) * count, count, newTimeCursor, uid) //val result: Future[Seq[VideoRow]] = videoDAO.refreshVideoByChannel(uid, chid, (page - 1) * count, count, newTimeCursor, nid)
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress, uid)

      val response = for {
        r <- result.map { seq =>
          seq.map { news =>
            toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
          }
        }
        ad <- adFO
      } yield {
        r.take(count.toInt - 1) ++: ad
      }

      //插入已浏览表
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now(), Some(6), Some(44)) } }
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

      response.map { seq =>
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {

          var seccount = 10
          val newsfeed = seq.map { news =>
            seccount = seccount - 1
            news.copy(ptime = newTimeCursor.plusSeconds(seccount))
          }
          changeADtime(newsfeed, newTimeCursor).take(count.toInt).map { r =>
            if (r.rtype.getOrElse(0) == 3)
              r.copy(style = 11)
            else
              r
          }
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within VideoService.refreshFeedWithAd($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)

      val result = newsResponseDao.video((page - 1) * count, count, newTimeCursor, uid) //videoDAO.loadVideoByChannel(uid, chid, (page - 1) * count, count, newTimeCursor, nid)
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress, uid)

      val response = for {
        r <- result.map { seq =>
          seq.map { news =>
            toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
          }
        }
        ad <- adFO
      } yield {
        r.take(count.toInt - 1) ++: ad
      }

      //插入已浏览表
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now(), Some(6), Some(44)) } }
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

      response.map { seq =>
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          var seccount = 0
          val newsfeed = seq.map { news =>
            seccount = seccount - 1
            news.copy(ptime = newTimeCursor.plusSeconds(seccount))
          }
          changeADtime(newsfeed, newTimeCursor).take(count.toInt).map { r =>
            if (r.rtype.getOrElse(0) == 3)
              r.copy(style = 11)
            else
              r
          }
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within VideoService.loadFeedWithAd($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def changeADtime(feeds: Seq[NewsFeedResponse], newTimeCursor: LocalDateTime): Seq[NewsFeedResponse] = {
    //-----------将广告放在第六个-----------
    //第六个新闻nid和时间
    val nid6 = feeds.take(6).lastOption match {
      case Some(news) => news.nid
      case _          => 0L
    }
    val time6 = feeds.take(6).lastOption match {
      case Some(news) => news.ptime
      case _          => newTimeCursor
    }
    //广告的时间
    val timead = feeds.filter(_.rtype == Some(3)).headOption match {
      case Some(news) => news.ptime
      case _          => newTimeCursor
    }
    //广告时间和第六条新闻时间互换
    feeds.map { news =>
      if (news.rtype == Some(3)) {
        news.copy(ptime = time6)
      } else if (news.nid == nid6) {
        news.copy(ptime = timead)
      } else {
        news
      }
    }.sortBy(_.ptime)
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    val nowTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(0))
    if (reqTimeCursor.isBefore(oldTimeCursor) || reqTimeCursor.isAfter(nowTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def toNewsFeedResponse(nid: Long, url: String, docid: String, title: String, pname: Option[String], purl: Option[String],
                         collect: Int, concern: Int, comment: Int, inum: Int, style: Int, imgs: Option[String], state: Int,
                         ctime: Timestamp, chid: Long, icon: Option[String], videourl: Option[String], thumbnail: Option[String],
                         duration: Option[Int], rtype: Option[Int]): NewsFeedResponse = {
    val imgsList = imgs match {
      case Some(str) =>
        Some(str.replace("{", "").replace("}", "").split(",").toList)
      case _ => None
    }

    val date = new Date(ctime.getTime)
    val newsSimpleRowBase = NewsSimpleRowBase(Some(nid), url, docid, title, None, LocalDateTime.fromDateFields(date), pname, purl, None, None)
    val newsSimpleRowIncr = NewsSimpleRowIncr(collect, concern, comment, inum, style, imgsList)
    val newsSimpleRowSyst = NewsSimpleRowSyst(state, LocalDateTime.fromDateFields(date), chid, None, icon, rtype, videourl, thumbnail, duration, Some(6), Some(44))
    val newsSimpleRow = NewsSimpleRow(newsSimpleRowBase, newsSimpleRowIncr, newsSimpleRowSyst)
    NewsFeedResponse.from(newsSimpleRow)
  }

  def findDetailsWithProfileByNid(nid: Long, uidOpt: Option[Long]): Future[Option[NewsDetailsResponse]] = {
    val result: Future[Option[NewsDetailsResponse]] = uidOpt match {
      case None => videoDAO.findByNid(nid).map {
        case Some(row) =>
          var detail = NewsDetailsResponse.from1(row)
          detail.content.\\("imag")
          Some(NewsDetailsResponse.from1(row))
        case _ => None
      }
      case Some(uid) => videoDAO.findByNidWithProfile(nid, uid).map {
        case Some((row, c1, c2, c3)) => Some(NewsDetailsResponse.from1(row, Some(c1), Some(c2), Some(c3)))
        case _                       => None
      }
    }
    result.recover {
      case NonFatal(e) =>
        Logger.error(s"Within VideoService.findDetailsWithProfileByNid($nid, $uidOpt): ${e.getMessage}")
        None
    }
  }

}