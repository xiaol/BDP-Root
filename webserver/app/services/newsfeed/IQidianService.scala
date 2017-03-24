package services.newsfeed

import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import commons.utils.JodaOderingImplicits
import commons.utils.JodaOderingImplicits.LocalDateTimeReverseOrdering
import commons.utils.JodaUtils._
import dao.news._
import dao.newsfeed.NewsFeedDao
import dao.userprofiles.HateNewsDAO
import org.joda.time.LocalDateTime
import play.api.Logger
import services.advertisement.AdResponseService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.util.Random
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[QidianService])
trait IQidianService {
  def refreshQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]]
  def loadQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]]
}

class QidianService @Inject() (val newsUnionFeedDao: NewsUnionFeedDao, val newsResponseDao: NewsResponseDao, val topicListDAO: TopicListDAO, val adResponseService: AdResponseService,
                               val newsFeedDao: NewsFeedDao, val newsRecommendReadDAO: NewsRecommendReadDAO, val topicNewsReadDAO: TopicNewsReadDAO, val hateNewsDAO: HateNewsDAO) extends IQidianService {

  private def qidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      val level1 = count / 2
      val level2 = count / 5
      val level3 = 2
      val level4 = 1

      //----热点部分----
      //百度热词 和 有评论
      val refreshHotFO = newsUnionFeedDao.hot((page - 1) * count, level3, newTimeCursor, uid)

      //----推荐部分----
      //模型LDA 和 Kmeans推荐
      val refreshLDAandKmeansFO = newsUnionFeedDao.byLDAandKmeans((page - 1) * count, level1 / 2, newTimeCursor, uid)

      //根据用户偏好从人工选取 和 人工推荐(没有偏好数据时使用)
      val refreshByPeopleRecommendWithClickFO = newsUnionFeedDao.byPeopleRecommendWithClick((page - 1) * count, level2 + 1, newTimeCursor, uid)

      //-------补全新闻------补全流程:(LDA + Kmeans)没有时, 出点击量高的新闻 --> 普通新闻
      val refreshCommonFO = newsUnionFeedDao.common((page - 1) * count, count, newTimeCursor, uid)

      //----大图和视频部分----
      //大图新闻 和 视频
      val refreshBigImageAndVideo = v match {
        case Some(video: Int) if video == 1 => newsUnionFeedDao.byBigImageAndVideo((page - 1) * count, level4, newTimeCursor, uid)
        case _                              => newsUnionFeedDao.byBigImage((page - 1) * count, level4, newTimeCursor, uid)
      }

      //专题
      val topicsFO: Future[Seq[TopicList]] = t match {
        case 1 => topicListDAO.topicShow(uid).map(_.take(level4))
        case _ => Future.successful { Seq[TopicList]() }
      }

      //不感兴趣新闻,获取来源和频道
      val hateNews: Future[Seq[NewsRow]] = hateNewsDAO.getNewsByUid(uid)

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val result: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { seq =>
          seq.map { newsFeedRow =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(newsFeedRow)
            newsFeedResponse
          }
        }

        lDAandKmeans <- refreshLDAandKmeansFO.map { seq =>
          seq.map { newsFeedRow =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(newsFeedRow)
            newsFeedResponse
          }
        }

        peopleRecommendWithClick <- refreshByPeopleRecommendWithClickFO.map { seq =>
          seq.map { newsFeedRow =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(newsFeedRow)
            newsFeedResponse
          }
        }
        commons <- refreshCommonFO.map { seq =>
          seq.map { newsFeedRow =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(newsFeedRow)
            newsFeedResponse
          }
        }
        bigimg5 <- refreshBigImageAndVideo.map { seq =>
          seq.filter(_.rtype == Some(999)).map { newsFeedRow =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(newsFeedRow)
            newsFeedResponse
          }
        }

        video <- refreshBigImageAndVideo.map { seq =>
          seq.filter(_.rtype == Some(6)).take(v.getOrElse(0)).map { newsFeedRow =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(newsFeedRow)
            newsFeedResponse
          }
        }

        topics <- topicsFO.map {
          case seq: Seq[TopicList] => seq.map { topic =>
            NewsFeedResponse.from(topic)
          }
        }

        hatePnameWithChid <- hateNews

      } yield {
        (bigimg5.take(level4) ++: topics
          ++: lDAandKmeans.take(level1.toInt) ++: hots.take(level3) ++: video ++: peopleRecommendWithClick.take((level2 * 2).toInt)
          ++: commons).filter { feed =>
            var flag = true
            hatePnameWithChid.foreach { news =>
              if (news.base.pname.getOrElse("1").equals(feed.pname.getOrElse("2"))) {
                flag = false
              }
            }
            flag
          }.take(count.toInt + 2)
      }

      result

    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianService.qidian($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      val newsFO: Future[Seq[NewsFeedResponse]] = qidian(uid, page, count, timeCursor, t, v)

      val aaa = Await.result(newsFO, Duration(5000, TimeUnit.MILLISECONDS))
      //广告
      val adFO: Future[Seq[NewsFeedResponse]] = adbody match {
        case Some(body: String) => adResponseService.getAdResponse(body, remoteAddress, uid)
        case _                  => Future.successful(Seq[NewsFeedResponse]())
      }

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val r: Future[Seq[NewsFeedResponse]] = for {
        news <- newsFO
        ad <- adFO
      } yield {
        (ad ++: news).take(count.toInt + 2)
      }
      //规则一:去重复新闻,一个来源可能重复
      //规则二:重做时间
      //规则三:过滤连续重复来源
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>

        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内,同一来源时间不能一样,否则后端排好序,APP端重排序时可能排在一起
        var nums: Map[String, LocalDateTime] = Map()
        seq.map { n =>
          if (n.rtype.getOrElse(0) == 41) {
            //专题置顶
            n.copy(ptime = newTimeCursor.plusSeconds(10)).copy(rtype = Some(4))
          } else if (n.rtype.getOrElse(0) == 999 && n.style > 10) {
            //5等级大图新闻
            n.copy(ptime = newTimeCursor.plusSeconds(9))
          } else if (n.rtype.getOrElse(0) == 999) {
            //5等级非大图
            n.copy(ptime = newTimeCursor.plusSeconds(8))
          } else if (n.rtype.getOrElse(0) == 3) {
            //广告
            n.copy(ptime = newTimeCursor.plusSeconds(6))
          } else {
            n.copy(ptime = newTimeCursor.plusSeconds(Random.nextInt(6)))
          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻,去除连续出现同一来源(要放在改时间之后,要不然有可能再排序到一起)
        val feeds = seq.sortBy(_.ptime).filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
        //-----------将广告放在第六个-----------
        //将广告放在第6个
        var seccount = 10
        val newsfeed = feeds.map { news =>
          seccount = seccount - 1
          news.copy(ptime = newTimeCursor.plusSeconds(seccount))
        }
        changeADtime(newsfeed, newTimeCursor)
      }.map(_.take(count.toInt))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      if (uid != 6440748) {
        println("uid==========" + uid)
        //从结果中取要浏览的20条,插入已浏览表中
        newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
        newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }
        //专题
        result.map { seq => seq.filter(_.rtype.getOrElse(0) == 4).map { v => topicNewsReadDAO.insertByTid(uid, v.nid.toInt) } }
      }

      //将99的改回
      result.map { seq =>
        val feed = seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 999)
            r.copy(rtype = Some(2))
          else
            r
        }
        //若只有广告,返回空
        if (feed.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          feed
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianService.refresh($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)

      val newsFO: Future[Seq[NewsFeedResponse]] = qidian(uid, page, count, timeCursor, t, v)
      //广告
      val adFO: Future[Seq[NewsFeedResponse]] = adbody match {
        case Some(body: String) => adResponseService.getAdResponse(body, remoteAddress, uid)
        case _                  => Future.successful(Seq[NewsFeedResponse]())
      }

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val r: Future[Seq[NewsFeedResponse]] = for {
        news <- newsFO
        ad <- adFO
      } yield {
        (ad ++: news).take(count.toInt + 2)
      }
      //规则一:去重复新闻,一个来源可能重复
      //规则二:重做时间
      //规则三:过滤连续重复来源
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>

        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)
        var flag = true
        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.sortBy(_.ptime).map { n =>
          if (n.rtype.getOrElse(0) == 999) {
            flag = false
            n.copy(ptime = localDateTime.plusSeconds(-1))
          } else if (n.rtype.getOrElse(0) == 4) {
            flag = false
            n.copy(ptime = localDateTime.plusSeconds(-2))
          } else if (n.rtype.getOrElse(0) == 3) {
            n.copy(ptime = localDateTime.plusSeconds(-3))
          } else {
            if (flag) {
              flag = false
              n.copy(ptime = localDateTime.plusSeconds(-2))
            } else {
              var newtime = localDateTime.plusSeconds(Random.nextInt(10) - 15)
              n.copy(ptime = newtime)
            }

          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻,去除连续出现同一来源(要放在改时间之后,要不然有可能再排序到一起)
        val feeds = seq.sortBy(_.ptime).filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
        //-----------将广告放在第六个-----------
        //将广告放在第6个
        var seccount = 0
        val newsfeed = feeds.map { news =>
          seccount = seccount - 1
          news.copy(ptime = newTimeCursor.plusSeconds(seccount))
        }
        changeADtime(newsfeed, newTimeCursor)
      }.map(_.take(count.toInt))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }
      //专题
      result.map { seq => seq.filter(_.rtype.getOrElse(0) == 4).map { v => topicNewsReadDAO.insertByTid(uid, v.nid.toInt) } }
      //将99的改回
      result.map { seq =>
        val feed = seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 999)
            r.copy(rtype = Some(2))
          else
            r
        }
        //若只有广告,返回空
        if (feed.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          feed
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianService.loadQidian($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def toNewsFeedResponse(newsFeedRow: NewsFeedRow): NewsFeedResponse = {
    val imgsList = newsFeedRow.imgs match {
      case Some(str) =>
        Some(str.split(",").toList)
      case _ => None
    }

    val thumbnail = newsFeedRow.rtype match {
      case Some(newstype) if newstype == 6 => imgsList match {
        case Some(list) => Some(list.head)
        case _          => None
      }
      case _ => None
    }

    NewsFeedResponse(newsFeedRow.nid, newsFeedRow.docid, newsFeedRow.title, LocalDateTime.now(), newsFeedRow.pname, newsFeedRow.purl, newsFeedRow.chid,
      newsFeedRow.collect, newsFeedRow.concern, newsFeedRow.un_concern, newsFeedRow.comment, newsFeedRow.style,
      imgsList, newsFeedRow.rtype, None, newsFeedRow.icon, newsFeedRow.videourl, thumbnail, newsFeedRow.duration, None, newsFeedRow.logtype, Some(1))
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    if (reqTimeCursor.isBefore(oldTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def changeADtime(feeds: Seq[NewsFeedResponse], newTimeCursor: LocalDateTime): Seq[NewsFeedResponse] = {
    //-----------将广告放在第六个-----------
    if (feeds.filter(_.rtype == Some(3)).length > 0) {
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
    } else {
      feeds
    }
  }

}
