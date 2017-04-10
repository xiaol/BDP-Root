package services.video

import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.{ ExtendData, _ }
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
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdNewsFeedResponse(adbody, remoteAddress)

      val response = for {
        r <- result.map { seq =>
          seq.map { newsFeedRow =>
            toNewsFeedResponse(newsFeedRow)
          }
        }
        ad <- adFO
      } yield {
        ad ++: r
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
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdNewsFeedResponse(adbody, remoteAddress)

      val response = for {
        r <- result.map { seq =>
          seq.map { newsFeedRow =>
            toNewsFeedResponse(newsFeedRow)
          }
        }
        ad <- adFO
      } yield {
        ad ++: r
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
    if (reqTimeCursor.isBefore(oldTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def toNewsFeedResponse(newsFeedRow: NewsFeedRow): NewsFeedResponse = {
    val imgsList = newsFeedRow.imgs match {
      case Some(str) =>
        Some(str.split(",").toList)
      case _ => None
    }

    //修改评论数
    var commentnum = newsFeedRow.comment
    if (commentnum > 10 && commentnum <= 70) {
      commentnum = commentnum * 2
    } else if (commentnum > 70 && commentnum <= 200) {
      commentnum = commentnum * 13
    } else if (commentnum > 200) {
      commentnum = commentnum * 61
    }

    //    val thumbnail = newsFeedRow.rtype match {
    //      case Some(newstype) if newstype == 6 => imgsList match {
    //        case Some(list) => Some(list.head)
    //        case _          => None
    //      }
    //      case _ => None
    //    }

    NewsFeedResponse(newsFeedRow.nid, newsFeedRow.docid, newsFeedRow.title, LocalDateTime.now(), newsFeedRow.pname, newsFeedRow.purl, newsFeedRow.chid,
      newsFeedRow.concern, newsFeedRow.un_concern, commentnum, newsFeedRow.style,
      None, newsFeedRow.rtype, None, newsFeedRow.icon, newsFeedRow.videourl, newsFeedRow.thumbnail, newsFeedRow.duration, None, newsFeedRow.rtype, Some(newsFeedRow.chid.toInt), Some(ExtendData(newsFeedRow.nid, newsFeedRow.clicktimes)))
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