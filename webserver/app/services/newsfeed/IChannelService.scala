package services.newsfeed

import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import dao.news._
import dao.newsfeed.NewsFeedDao
import org.joda.time.LocalDateTime
import play.api.Logger
import services.advertisement.AdResponseService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[FeedChannelService])
trait IFeedChannelService {
  //带广告频道新闻
  def refreshFeedByChannelWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def loadFeedByChannelWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]]

  //不带广告频道新闻
  def refreshFeedByChannel(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def loadFeedByChannel(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]

}

class FeedChannelService @Inject() (val adResponseService: AdResponseService, val newsFeedDao: NewsFeedDao, val newsResponseDao: NewsResponseDao) extends IFeedChannelService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def refreshFeedByChannelWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
      val date = new Date(timeCursor)
      val localDateTime = LocalDateTime.fromDateFields(date)

      val result = sechidOpt match {
        case Some(sechid) => newsResponseDao.bySeChannel((page - 1) * count, count, newTimeCursor, uid, chid, sechid)
        case None         => newsResponseDao.byChannel((page - 1) * count, count, newTimeCursor, uid, chid)
      }

      val resultKM = newsResponseDao.byChannelWithKmeans((page - 1) * count, count / 2, newTimeCursor, uid, chid)

      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress, uid)

      val response = for {
        r <- result.map { seq =>
          seq.map { news =>
            toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
          }
        }
        rkm <- resultKM.map { seq =>
          seq.map { news =>
            toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
          }
        }
        ad <- adFO
      } yield {
        (rkm ++: ad ++: r).take(count.toInt)
      }

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

      response.map { seq =>
        var flag = true
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          //将广告时间随机成任意一条新闻时间
          seq.map { r =>
            if (r.rtype.getOrElse(0) == 3) {
              r.copy(ptime = newTimeCursor.plusSeconds(6))
            } else if (flag) {
              flag = false
              r.copy(ptime = newTimeCursor.plusSeconds(7))
            } else {
              r.copy(ptime = newTimeCursor.plusSeconds(Random.nextInt(5)))
            }

          }.sortBy(_.ptime)
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByChannelWithAd($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByChannelWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
      val result = sechidOpt match {
        case Some(sechid) => newsResponseDao.bySeChannel((page - 1) * count, count, newTimeCursor, uid, chid, sechid)
        case None         => newsResponseDao.byChannel((page - 1) * count, count, newTimeCursor, uid, chid)
      }

      val resultKM = newsResponseDao.byChannelWithKmeans((page - 1) * count, count / 2, newTimeCursor, uid, chid)

      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress, uid)

      val response = for {
        r <- result.map { seq =>
          seq.map { news =>
            toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
          }
        }
        rkm <- resultKM.map { seq =>
          seq.map { news =>
            toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
          }
        }
        ad <- adFO
      } yield {
        (rkm ++: ad ++: r).take(count.toInt)
      }

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

      response.map { seq =>
        var flag = true
        val localDateTime = msecondsToDatetime(timeCursor)
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          //将广告时间放第二条
          seq.map { r =>
            if (r.rtype.getOrElse(0) == 3) {
              r.copy(ptime = localDateTime.plusSeconds(-2))
            } else if (flag) {
              flag = false
              r.copy(ptime = localDateTime.plusSeconds(-1))
            } else {
              r.copy(ptime = localDateTime.plusSeconds(Random.nextInt(5) - 7))
            }

          }.sortBy(_.ptime).take(count.toInt)
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByChannelWithAd($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByChannel(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

    val result = sechidOpt match {
      case Some(sechid) => newsResponseDao.bySeChannel((page - 1) * count, count, newTimeCursor, uid, chid, sechid)
      case None         => newsResponseDao.byChannel((page - 1) * count, count, newTimeCursor, uid, chid)
    }

    val response = result.map { seq =>
      seq.map { news =>
        toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
      }
    }

    val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
    //从结果中取要浏览的20条,插入已浏览表中
    newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

    response.map { seq =>
      var flag = true
      //若只有广告,返回空
      if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
        //将广告时间随机成任意一条新闻时间
        seq.map { r =>
          if (r.rtype.getOrElse(0) == 3) {
            r.copy(ptime = newTimeCursor.plusSeconds(6))
          } else if (flag) {
            flag = false
            r.copy(ptime = newTimeCursor.plusSeconds(7))
          } else {
            r.copy(ptime = newTimeCursor.plusSeconds(Random.nextInt(5)))
          }
        }.sortBy(_.ptime)
      } else {
        Seq[NewsFeedResponse]()
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByChannel(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val result = sechidOpt match {
      case Some(sechid) => newsResponseDao.bySeChannel((page - 1) * count, count, newTimeCursor, uid, chid, sechid)
      case None         => newsResponseDao.byChannel((page - 1) * count, count, newTimeCursor, uid, chid)
    }

    val response = result.map { seq =>
      seq.map { news =>
        toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
      }
    }

    val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
    //从结果中取要浏览的20条,插入已浏览表中
    newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

    response.map { seq =>
      var flag = true
      val localDateTime = msecondsToDatetime(timeCursor)
      //若只有广告,返回空
      if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
        //将广告时间放第二条
        seq.map { r =>
          if (r.rtype.getOrElse(0) == 3) {
            r.copy(ptime = localDateTime.plusSeconds(-2))
          } else if (flag) {
            flag = false
            r.copy(ptime = localDateTime.plusSeconds(-1))
          } else {
            r.copy(ptime = localDateTime.plusSeconds(Random.nextInt(5) - 7))
          }

        }.sortBy(_.ptime).take(count.toInt)
      } else {
        Seq[NewsFeedResponse]()
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
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
    val newsSimpleRowSyst = NewsSimpleRowSyst(state, LocalDateTime.fromDateFields(date), chid, None, icon, rtype, videourl, thumbnail, duration)
    val newsSimpleRow = NewsSimpleRow(newsSimpleRowBase, newsSimpleRowIncr, newsSimpleRowSyst)
    NewsFeedResponse.from(newsSimpleRow)
  }
}
