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
import dao.userprofiles.HateNewsDAO
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

class FeedChannelService @Inject() (val adResponseService: AdResponseService, val newsFeedDao: NewsFeedDao, val newsResponseDao: NewsResponseDao, val newsRecommendReadDAO: NewsRecommendReadDAO, val hateNewsDAO: HateNewsDAO) extends IFeedChannelService {

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

      //      val resultKM = newsResponseDao.byChannelWithKmeans((page - 1) * count, count / 2, newTimeCursor, uid, chid)

      //不感兴趣新闻,获取来源和频道
      val hateNews: Future[Seq[NewsRow]] = hateNewsDAO.getNewsByUid(uid)

      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdNewsFeedResponse(adbody, remoteAddress)

      val response = for {
        r <- result.map { seq =>
          seq.map { newsFeedRow =>
            toNewsFeedResponse(newsFeedRow)
          }
        }
        //        rkm <- resultKM.map { seq =>
        //          seq.map { news =>
        //            toNewsFeedResponse(newsFeedRow)
        //          }
        //        }
        ad <- adFO
        hatePnameWithChid <- hateNews
      } yield {
        ad ++: (r.filter { feed =>
          var flag = true
          hatePnameWithChid.foreach { news =>
            if (news.base.pname.getOrElse("1").equals(feed.pname.getOrElse("2"))) {
              flag = false
            }
          }
          flag
        })
      }

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now(), Some(0), Some(chid.toInt)) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

      response.map { seq =>
        var flag = true
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          //将广告放在第6个
          var seccount = 10
          val newsfeed = seq.map { news =>
            seccount = seccount - 1
            news.copy(ptime = newTimeCursor.plusSeconds(seccount))
          }
          changeADtime(newsfeed, newTimeCursor).take(count.toInt)
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within FeedChannelService.refreshFeedByChannelWithAd($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
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

  def loadFeedByChannelWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
      val result = sechidOpt match {
        case Some(sechid) => newsResponseDao.bySeChannel((page - 1) * count, count, newTimeCursor, uid, chid, sechid)
        case None         => newsResponseDao.byChannel((page - 1) * count, count, newTimeCursor, uid, chid)
      }

      //      val resultKM = newsResponseDao.byChannelWithKmeans((page - 1) * count, count / 2, newTimeCursor, uid, chid)

      //不感兴趣新闻,获取来源和频道
      val hateNews: Future[Seq[NewsRow]] = hateNewsDAO.getNewsByUid(uid)

      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdNewsFeedResponse(adbody, remoteAddress)

      val response = for {
        r <- result.map { seq =>
          seq.map { newsFeedRow =>
            toNewsFeedResponse(newsFeedRow)
          }
        }
        //        rkm <- resultKM.map { seq =>
        //          seq.map { news =>
        //            toNewsFeedResponse(newsFeedRow)
        //          }
        //        }
        ad <- adFO
        hatePnameWithChid <- hateNews
      } yield {
        (ad ++: r).filter { feed =>
          var flag = true
          hatePnameWithChid.foreach { news =>
            if (news.base.pname.getOrElse("1").equals(feed.pname.getOrElse("2"))) {
              flag = false
            }
          }
          flag
        }.take(count.toInt)
      }

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now(), Some(0), Some(chid.toInt)) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

      response.map { seq =>
        var flag = true
        val localDateTime = msecondsToDatetime(timeCursor)
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          //将广告放在第6个
          var seccount = 0
          val newsfeed = seq.map { news =>
            seccount = seccount - 1
            news.copy(ptime = newTimeCursor.plusSeconds(seccount))
          }
          changeADtime(newsfeed, newTimeCursor).take(count.toInt)
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within FeedChannelService.loadFeedByChannelWithAd($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
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
      seq.map { newsFeedRow =>
        toNewsFeedResponse(newsFeedRow)
      }
    }

    val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now(), Some(0), Some(chid.toInt)) } }
    //从结果中取要浏览的20条,插入已浏览表中
    newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
    newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

    response.map { seq =>
      var flag = true
      //若只有广告,返回空
      if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
        //将广告放在第6个
        var seccount = 10
        val newsfeed = seq.map { news =>
          seccount = seccount - 1
          news.copy(ptime = newTimeCursor.plusSeconds(seccount))
        }
        changeADtime(newsfeed, newTimeCursor).take(count.toInt)
      } else {
        Seq[NewsFeedResponse]()
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within FeedChannelService.refreshFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
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
      seq.map { newsFeedRow =>
        toNewsFeedResponse(newsFeedRow)
      }
    }

    val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now(), Some(0), Some(chid.toInt)) } }
    //从结果中取要浏览的20条,插入已浏览表中
    newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
    newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }

    response.map { seq =>
      var flag = true
      val localDateTime = msecondsToDatetime(timeCursor)
      //若只有广告,返回空
      if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
        //将广告放在第6个
        var seccount = 0
        val newsfeed = seq.map { news =>
          seccount = seccount - 1
          news.copy(ptime = newTimeCursor.plusSeconds(seccount))
        }
        changeADtime(newsfeed, newTimeCursor).take(count.toInt)
      } else {
        Seq[NewsFeedResponse]()
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within FeedChannelService.loadFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
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
      newsFeedRow.collect, newsFeedRow.concern, newsFeedRow.un_concern, commentnum, newsFeedRow.style,
      imgsList, newsFeedRow.rtype, None, newsFeedRow.icon, newsFeedRow.videourl, newsFeedRow.thumbnail, newsFeedRow.duration, None, Some(0), Some(newsFeedRow.chid.toInt))
  }
}
