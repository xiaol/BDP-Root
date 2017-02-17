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
import JodaOderingImplicits.LocalDateTimeReverseOrdering

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[QidianService])
trait IQidianService {
  def refreshQidianWithAd(uid: Long, page: Long, count: Long, timeCursor: Long, adbody: String, t: Int, remoteAddress: Option[String], v: Option[Int]): Future[Seq[NewsFeedResponse]]
  def loadQidianWithAd(uid: Long, page: Long, count: Long, timeCursor: Long, adbody: String, t: Int, remoteAddress: Option[String], v: Option[Int]): Future[Seq[NewsFeedResponse]]
  def refreshQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int): Future[Seq[NewsFeedResponse]]
  def loadQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int): Future[Seq[NewsFeedResponse]]
}

class QidianService @Inject() (val newsResponseDao: NewsResponseDao, val topicListDAO: TopicListDAO, val adResponseService: AdResponseService,
                               val newsFeedDao: NewsFeedDao, val topicNewsReadDAO: TopicNewsReadDAO) extends IQidianService {

  def refreshQidianWithAd(uid: Long, page: Long, count: Long, timeCursor: Long, adbody: String, t: Int, remoteAddress: Option[String], v: Option[Int]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      val level1 = count / 2
      val level2 = count / 5
      val level3 = 2
      val level4 = 1

      //----热点部分----
      //有评论
      val refreshHotFO = newsResponseDao.hot((page - 1) * count, level2, newTimeCursor, uid)
      //百度热词
      val refreshHotWordFO = newsResponseDao.baiduHotWord((page - 1) * count, level2, newTimeCursor, uid)

      //----推荐部分----
      //模型推荐
      val refreshLDARecommendFO = newsResponseDao.byLDA((page - 1) * count, level1 / 2, newTimeCursor, uid)
      val refreshKMeansRecommendFO = newsResponseDao.byKmeans((page - 1) * count, level1, newTimeCursor, uid)
      //人工推荐
      val refreshByPeopleRecommendFO = newsResponseDao.byPeopleRecommend((page - 1) * count, level2 + 1, newTimeCursor, uid)

      //-------普通新闻------(可能情况:其他都没有数据,全出普通)
      val refreshCommonFO = newsResponseDao.common((page - 1) * count, count, newTimeCursor, uid)

      //----大图部分----
      //大图新闻
      val refreshBigImgFO5 = newsResponseDao.byBigImage((page - 1) * count, level4, newTimeCursor, uid)

      //专题
      val topicsFO: Future[Seq[TopicList]] = t match {
        case 1 => topicListDAO.topicShow(uid).map(_.take(level4))
        case _ => Future.successful { Seq[TopicList]() }
      }

      //视频
      val videoFO = newsResponseDao.video((page - 1) * count, level4, newTimeCursor, uid)

      //广告
      val body: String = adbody
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(body, remoteAddress, uid)

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(1))
          }
        }
        hotWords <- refreshHotWordFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(1))
          }
        }
        lDARecommend <- refreshLDARecommendFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(0))
          }
        }
        kMeansRecommend <- refreshKMeansRecommendFO.map {
          seq =>
            seq.map { news =>
              val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
              newsFeedResponse.copy(rtype = Some(0))
            }
        }
        peopleRecommend <- refreshByPeopleRecommendFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(2))
          }
        }
        commons <- refreshCommonFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(0))
          }
        }
        bigimg5 <- refreshBigImgFO5.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(999))
          }
        }

        topics <- topicsFO.map { case seq: Seq[TopicList] => seq.map { topic => NewsFeedResponse.from(topic) } }
        video <- videoFO.map { seq =>
          seq.take(v.getOrElse(0)).map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse
          }
        }
        ad <- adFO
      } yield {
        (bigimg5.take(level4) ++: topics ++: ad
          ++: (hotWords ++: hots).take((level2).toInt) ++: video ++: ((lDARecommend ++: kMeansRecommend).take(level1.toInt) ++: peopleRecommend.take((level2 * 2).toInt)).take(count.toInt)
          ++: commons).take(count.toInt + 2)
      }
      //规则一:去重复新闻,一个来源可能重复
      //规则二:重做时间
      //规则三:过滤连续重复来源
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>

        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内,同一来源时间不能一样,否则后端排好序,APP端重排序时可能排在一起
        var nums: Map[String, LocalDateTime] = Map()
        var flag = true
        seq.map { n =>
          if (n.rtype.getOrElse(0) == 41) {
            //专题置顶
            flag = false
            n.copy(ptime = newTimeCursor.plusSeconds(10)).copy(rtype = Some(4))
          } else if (n.rtype.getOrElse(0) == 999 && n.style > 10) {
            //5等级大图新闻
            flag = false
            n.copy(ptime = newTimeCursor.plusSeconds(9))
          } else if (n.rtype.getOrElse(0) == 999) {
            //5等级非大图
            flag = false
            n.copy(ptime = newTimeCursor.plusSeconds(8))
          } else if (n.rtype.getOrElse(0) == 3) {
            //广告
            n.copy(ptime = newTimeCursor.plusSeconds(6))
          } else {
            if (flag) {
              flag = false
              n.copy(ptime = newTimeCursor.plusSeconds(7))
            } else {
              n.copy(ptime = newTimeCursor.plusSeconds(Random.nextInt(6)))
            }

          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻,去除连续出现同一来源(要放在改时间之后,要不然有可能再排序到一起)
        seq.sortBy(_.ptime).filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
      }.map(_.take(count.toInt))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
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
        Logger.error(s"Within NewsRecommendService.refreshFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadQidianWithAd(uid: Long, page: Long, count: Long, timeCursor: Long, adbody: String, t: Int, remoteAddress: Option[String], v: Option[Int]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)

      val level1 = count / 2
      val level2 = count / 5
      val level3 = 2
      val level4 = 1

      //----热点部分----
      //有评论
      val refreshHotFO = newsResponseDao.hot((page - 1) * count, level2, newTimeCursor, uid)
      //百度热词
      val refreshHotWordFO = newsResponseDao.baiduHotWord((page - 1) * count, level2, newTimeCursor, uid)

      //----推荐部分----
      //模型推荐
      val refreshLDARecommendFO = newsResponseDao.byLDA((page - 1) * count, level1 / 2, newTimeCursor, uid)
      val refreshKMeansRecommendFO = newsResponseDao.byKmeans((page - 1) * count, level1, newTimeCursor, uid)
      //人工推荐
      val refreshByPeopleRecommendFO = newsResponseDao.byPeopleRecommend((page - 1) * count, level2 + 1, newTimeCursor, uid)

      //-------普通新闻------(可能情况:其他都没有数据,全出普通)
      val refreshCommonFO = newsResponseDao.common((page - 1) * count, count, newTimeCursor, uid)

      //----大图部分----
      //大图新闻
      val refreshBigImgFO5 = newsResponseDao.byBigImage((page - 1) * count, level4, newTimeCursor, uid)

      //专题
      val topicsFO: Future[Seq[TopicList]] = t match {
        case 1 => topicListDAO.topicShow(uid).map(_.take(level4))
        case _ => Future.successful { Seq[TopicList]() }
      }

      //视频
      val videoFO = newsResponseDao.video((page - 1) * count, level4, newTimeCursor, uid)

      val body: String = adbody
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(body, remoteAddress, uid)

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(1))
          }
        }
        hotWords <- refreshHotWordFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(1))
          }
        }
        lDARecommend <- refreshLDARecommendFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(0))
          }
        }
        kMeansRecommend <- refreshKMeansRecommendFO.map {
          seq =>
            seq.map { news =>
              val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
              newsFeedResponse.copy(rtype = Some(0))
            }
        }
        peopleRecommend <- refreshByPeopleRecommendFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(2))
          }
        }
        commons <- refreshCommonFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(0))
          }
        }
        bigimg5 <- refreshBigImgFO5.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(999))
          }
        }

        topics <- topicsFO.map { case seq: Seq[TopicList] => seq.map { topic => NewsFeedResponse.from(topic) } }
        video <- videoFO.map { seq =>
          seq.take(v.getOrElse(0)).map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse
          }
        }
        ad <- adFO
      } yield {
        (bigimg5.take(level4) ++: topics ++: ad
          ++: (hotWords ++: hots).take((level2).toInt) ++: video ++: ((lDARecommend ++: kMeansRecommend).take(level1.toInt) ++: peopleRecommend.take((level2 * 2).toInt)).take(count.toInt)
          ++: commons).take(count.toInt + 2)
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
              var newtime = localDateTime.plusSeconds(Random.nextInt(3) - 10)
              n.copy(ptime = newtime)
            }

          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻,去除连续出现同一来源(要放在改时间之后,要不然有可能再排序到一起)
        seq.filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
      }.map(_.take(count.toInt))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
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
        Logger.error(s"Within NewsRecommendService.refreshFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)

      val level1 = count / 2
      val level2 = count / 5
      val level3 = 2
      val level4 = 1

      //----热点部分----
      //有评论
      val refreshHotFO = newsResponseDao.hot((page - 1) * count, level2, newTimeCursor, uid)
      //百度热词
      val refreshHotWordFO = newsResponseDao.baiduHotWord((page - 1) * count, level2, newTimeCursor, uid)

      //----推荐部分----
      //模型推荐
      val refreshLDARecommendFO = newsResponseDao.byLDA((page - 1) * count, level1 / 2, newTimeCursor, uid)
      val refreshKMeansRecommendFO = newsResponseDao.byKmeans((page - 1) * count, level1, newTimeCursor, uid)
      //人工推荐
      val refreshByPeopleRecommendFO = newsResponseDao.byPeopleRecommend((page - 1) * count, level2 + 1, newTimeCursor, uid)

      //-------普通新闻------(可能情况:其他都没有数据,全出普通)
      val refreshCommonFO = newsResponseDao.common((page - 1) * count, count, newTimeCursor, uid)

      //----大图部分----
      //大图新闻
      val refreshBigImgFO5 = newsResponseDao.byBigImage((page - 1) * count, level4, newTimeCursor, uid)

      //专题
      val topicsFO: Future[Seq[TopicList]] = t match {
        case 1 => topicListDAO.topicShow(uid).map(_.take(level4))
        case _ => Future.successful { Seq[TopicList]() }
      }

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(1))
          }
        }
        hotWords <- refreshHotWordFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(1))
          }
        }
        lDARecommend <- refreshLDARecommendFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(0))
          }
        }
        kMeansRecommend <- refreshKMeansRecommendFO.map {
          seq =>
            seq.map { news =>
              val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
              newsFeedResponse.copy(rtype = Some(0))
            }
        }
        peopleRecommend <- refreshByPeopleRecommendFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(2))
          }
        }
        commons <- refreshCommonFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(0))
          }
        }
        bigimg5 <- refreshBigImgFO5.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(999))
          }
        }

        topics <- topicsFO.map { case seq: Seq[TopicList] => seq.map { topic => NewsFeedResponse.from(topic) } }
      } yield {
        (bigimg5.take(level4) ++: topics
          ++: (hotWords ++: hots).take((level2).toInt) ++: ((lDARecommend ++: kMeansRecommend).take(level1.toInt) ++: peopleRecommend.take((level2 * 2).toInt)).take(count.toInt)
          ++: commons).take(count.toInt + 2)
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
            n.copy(ptime = localDateTime.plusSeconds(-1))
          } else if (n.rtype.getOrElse(0) == 4) {
            n.copy(ptime = localDateTime.plusSeconds(-2))
          } else if (n.rtype.getOrElse(0) == 3) {
            n.copy(ptime = localDateTime.plusSeconds(-3))
          } else {
            if (flag) {
              flag = false
              n.copy(ptime = localDateTime.plusSeconds(-2))
            } else {
              var newtime = localDateTime.plusSeconds(Random.nextInt(3) - 10)
              n.copy(ptime = newtime)
            }

          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻,去除连续出现同一来源(要放在改时间之后,要不然有可能再排序到一起)
        seq.filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
      }.map(_.take(count.toInt))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
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
        feed
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.refreshFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)

      val level1 = count / 2
      val level2 = count / 5
      val level3 = 2
      val level4 = 1

      //----热点部分----
      //有评论
      val refreshHotFO = newsResponseDao.hot((page - 1) * count, level2, newTimeCursor, uid)
      //百度热词
      val refreshHotWordFO = newsResponseDao.baiduHotWord((page - 1) * count, level2, newTimeCursor, uid)

      //----推荐部分----
      //模型推荐
      val refreshLDARecommendFO = newsResponseDao.byLDA((page - 1) * count, level1 / 2, newTimeCursor, uid)
      val refreshKMeansRecommendFO = newsResponseDao.byKmeans((page - 1) * count, level1, newTimeCursor, uid)
      //人工推荐
      val refreshByPeopleRecommendFO = newsResponseDao.byPeopleRecommend((page - 1) * count, level2 + 1, newTimeCursor, uid)

      //-------普通新闻------(可能情况:其他都没有数据,全出普通)
      val refreshCommonFO = newsResponseDao.common((page - 1) * count, count, newTimeCursor, uid)

      //----大图部分----
      //大图新闻
      val refreshBigImgFO5 = newsResponseDao.byBigImage((page - 1) * count, level4, newTimeCursor, uid)

      //专题
      val topicsFO: Future[Seq[TopicList]] = t match {
        case 1 => topicListDAO.topicShow(uid).map(_.take(level4))
        case _ => Future.successful { Seq[TopicList]() }
      }

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(1))
          }
        }
        hotWords <- refreshHotWordFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(1))
          }
        }
        lDARecommend <- refreshLDARecommendFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(0))
          }
        }
        kMeansRecommend <- refreshKMeansRecommendFO.map {
          seq =>
            seq.map { news =>
              val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
              newsFeedResponse.copy(rtype = Some(0))
            }
        }
        peopleRecommend <- refreshByPeopleRecommendFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(2))
          }
        }
        commons <- refreshCommonFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(0))
          }
        }
        bigimg5 <- refreshBigImgFO5.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20)
            newsFeedResponse.copy(rtype = Some(999))
          }
        }

        topics <- topicsFO.map { case seq: Seq[TopicList] => seq.map { topic => NewsFeedResponse.from(topic) } }
      } yield {
        (bigimg5.take(level4) ++: topics
          ++: (hotWords ++: hots).take((level2).toInt) ++: ((lDARecommend ++: kMeansRecommend).take(level1.toInt) ++: peopleRecommend.take((level2 * 2).toInt)).take(count.toInt)
          ++: commons).take(count.toInt + 2)
      }
      //规则一:去重复新闻,一个来源可能重复
      //规则二:重做时间
      //规则三:过滤连续重复来源
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>

        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)
        var flag = true
        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.map { n =>
          if (n.rtype.getOrElse(0) == 999) {
            n.copy(ptime = localDateTime.plusSeconds(-1))
          } else if (n.rtype.getOrElse(0) == 4) {
            n.copy(ptime = localDateTime.plusSeconds(-2))
          } else if (n.rtype.getOrElse(0) == 3) {
            n.copy(ptime = localDateTime.plusSeconds(-3))
          } else {
            if (flag) {
              flag = false
              n.copy(ptime = localDateTime.plusSeconds(-2))
            } else {
              var newtime = localDateTime.plusSeconds(Random.nextInt(3) - 10)
              n.copy(ptime = newtime)
            }

          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻,去除连续出现同一来源(要放在改时间之后,要不然有可能再排序到一起)
        seq.sortBy(_.ptime).filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
      }.map(_.take(count.toInt))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
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
        feed
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.refreshFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
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

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    if (reqTimeCursor.isBefore(oldTimeCursor)) oldTimeCursor else reqTimeCursor
  }

}
