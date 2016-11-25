package services.news

import java.util
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.{ NewsRecommendResponse, _ }
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import dao.news._
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
@ImplementedBy(classOf[NewsRecommendService])
trait INewsRecommendService {
  def operate(nid: Long, method: String, level: Option[Double], bigimg: Option[Int]): Future[Option[Long]]
  def insert(newsRecommend: NewsRecommend): Future[Option[Long]]
  def delete(nid: Long): Future[Option[Long]]
  def listNewsAndCountByRecommand(channel: Option[Long], ifrecommend: Int, page: Long, count: Long): Future[(Seq[NewsRecommendResponse], Long)]
  def listNewsByRecommand(channel: Option[Long], ifrecommend: Int, page: Long, count: Long): Future[Seq[NewsRecommendResponse]]
  def listNewsByRecommandCount(channel: Option[Long], ifrecommend: Int): Future[Int]
  def listNewsBySearch(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int): Future[(Seq[NewsRecommendResponse], Long)]
  def loadFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int): Future[Seq[NewsFeedResponse]]
  def refreshFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int): Future[Seq[NewsFeedResponse]]
  def listPublisherWithFlag(uid: Option[Long], keywords: String): Future[Seq[(NewsPublisherRow, Long)]]
}

class NewsRecommendService @Inject() (val newsRecommendDAO: NewsRecommendDAO, val newsEsService: NewsEsService, val adResponseService: AdResponseService,
                                      val newsDAO: NewsDAO, val newsRecommendReadDAO: NewsRecommendReadDAO, val newsrecommendclickDAO: NewsrecommendclickDAO,
                                      val topicListDAO: TopicListDAO, val topicNewsReadDAO: TopicNewsReadDAO) extends INewsRecommendService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def operate(nid: Long, method: String, level: Option[Double], bigimg: Option[Int]): Future[Option[Long]] = {
    if ("insert".equals(method)) {
      insert(NewsRecommend(nid, Some(LocalDateTime.now()), Some(level.getOrElse(1)), bigimg, Some(1)))
    } else if ("delete".equals(method)) {
      delete(nid: Long)
    } else {
      Future(None)
    }
  }

  def insert(newsRecommend: NewsRecommend): Future[Option[Long]] = {
    newsRecommendDAO.insert(newsRecommend).map { nid => Some(nid) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.insert(${newsRecommend.nid}) : ${e.getMessage}")
        None
    }
  }

  def delete(nid: Long): Future[Option[Long]] = {
    newsRecommendDAO.delete(nid).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.delete($nid): ${e.getMessage}")
        None
    }
  }

  def listNewsAndCountByRecommand(channel: Option[Long], ifrecommend: Int, page: Long, count: Long): Future[(Seq[NewsRecommendResponse], Long)] = {
    val newsRecommendResponses: Future[Seq[NewsRecommendResponse]] = listShowAndClickCount(channel, ifrecommend, page, count)
    val count1: Future[Int] = listNewsByRecommandCount(channel: Option[Long], ifrecommend: Int)
    for {
      n <- newsRecommendResponses
      c <- count1
    } yield (n, c.toLong)
  }

  def listShowAndClickCount(channel: Option[Long], ifrecommend: Int, page: Long, count: Long): Future[Seq[NewsRecommendResponse]] = {
    for {
      newsFeedResponses <- listNewsByRecommand(channel: Option[Long], ifrecommend: Int, page: Long, count: Long)
      showAndClickCount <- queryShowAndClickCount(newsFeedResponses.map(_.nid))
      newsFeedResponsesWithCount <- combineShowAndClickCount(newsFeedResponses, showAndClickCount)
    } yield (newsFeedResponsesWithCount)
  }

  def queryShowAndClickCount(nids: Seq[Long]) = {
    newsrecommendclickDAO.selectNewsrecommendclicks(nids)
  }

  def combineShowAndClickCount(newsFeedResponses: Seq[NewsRecommendResponse], showAndClickCount: Seq[(Long, Int, Int)]): Future[Seq[NewsRecommendResponse]] = {
    Future.successful {
      newsFeedResponses.map { news => news.copy(showcount = Some(showAndClickCount.filter(_._1 == news.nid).headOption.getOrElse(0, 0, 0)._2)).copy(clickcount = Some(showAndClickCount.filter(_._1 == news.nid).headOption.getOrElse(0, 0, 0)._3)) }
    }
  }

  def listNewsByRecommand(channel: Option[Long], ifrecommend: Int, page: Long, count: Long): Future[Seq[NewsRecommendResponse]] = {
    newsRecommendDAO.listNewsByRecommand(channel, ifrecommend, (page - 1) * count, count).map {
      case pairs: Seq[NewsRecommendResponse] => pairs.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.listNewsByRecommand($channel, $ifrecommend): ${e.getMessage}")
        Seq[NewsRecommendResponse]()
    }
  }

  def listNewsByRecommandCount(channel: Option[Long], ifrecommend: Int): Future[Int] = {
    newsRecommendDAO.listNewsByRecommandCount(channel, ifrecommend).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.listNewsByRecommandCount($channel, $ifrecommend): ${e.getMessage}")
        0
    }
  }

  //用户获取推荐新闻nid列表
  def listNewsBySearch(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int): Future[(Seq[NewsRecommendResponse], Long)] = {
    for {
      newsFeedResponses <- getNewsNids(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int)
      newsRecommends <- listNewsBySearch(newsFeedResponses._1)
      showAndClickCount <- queryShowAndClickCount(newsFeedResponses._1.map(_.nid))
      newsRecommendResponses <- listNewsBySearch(newsFeedResponses._1: Seq[NewsFeedResponse], newsRecommends: Seq[NewsRecommend])
      newsFeedResponsesWithCount <- combineShowAndClickCount(newsRecommendResponses, showAndClickCount)
    } yield (newsFeedResponsesWithCount, newsFeedResponses._2)
  }

  def getNewsNids(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int): Future[(Seq[NewsFeedResponse], Long)] = {
    newsEsService.search(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.getNewsNids(): ${e.getMessage}")
        (Seq[NewsFeedResponse](), 0L)
    }
  }

  def listNewsBySearch(nids: Seq[NewsFeedResponse]): Future[Seq[NewsRecommend]] = {
    newsRecommendDAO.listNewsBySearch(nids.map(_.nid)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.listNewsBySearch(): ${e.getMessage}")
        Seq[NewsRecommend]()
    }
  }

  def listNewsBySearch(newsFeedResponses: Seq[NewsFeedResponse], newsRecommends: Seq[NewsRecommend]): Future[Seq[NewsRecommendResponse]] = {
    var map = new util.HashMap[Long, NewsRecommend]()
    newsRecommends.foreach { newsRecommend =>
      map.put(newsRecommend.nid, newsRecommend)
    }
    val result = newsFeedResponses.map { newsFeedResponse =>
      if (map.containsKey(newsFeedResponse.nid)) {
        NewsRecommendResponse.from(newsFeedResponse, map.get(newsFeedResponse.nid))
      } else {
        NewsRecommendResponse.from(newsFeedResponse)
      }
    }
    Future(result)
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    if (reqTimeCursor.isBefore(oldTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def loadFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int): Future[Seq[NewsFeedResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByHot((page - 1) * count, count / 5, msecondsToDatetime(timeCursor), uid)
      val loadHotWordFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByHotWord((page - 1) * count, count / 10, msecondsToDatetime(timeCursor), uid)
      val loadModerRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByModelRecommend((page - 1) * count, (count - count / 4) / 2, msecondsToDatetime(timeCursor), uid)
      val loadByPeopleRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByPeopleRecommend((page - 1) * count, count / 4, msecondsToDatetime(timeCursor), uid)
      val loadCommonFO: Future[Seq[NewsRow]] = newsRecommendDAO.load((page - 1) * count, count, msecondsToDatetime(timeCursor), uid)
      val refreshRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, 2 * count / 4)
      val refreshByClickFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByClick((page - 1) * count, (count - count / 4) / 2 + 1, msecondsToDatetime(timeCursor), uid)

      val refreshByPeopleRecommendBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.refreshByPeopleRecommendBigImg(uid, 0, 1)
      val refreshBigImgFO5: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg5(uid, 0, 1)
      val refreshBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)

      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- loadHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) }
        hotWords <- loadHotWordFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) }
        moderRecommend <- loadModerRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        refreshByClick <- refreshByClickFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        peopleRecommend <- loadByPeopleRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(2)) }.sortBy(_.ptime) }
        commons <- loadCommonFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        recommends <- refreshRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(rtype = Some(2)) } }
        bigimg5 <- refreshBigImgFO5.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(999))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        peopleRecommendBigImg <- refreshByPeopleRecommendBigImgFO.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(2))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        bigimg <- refreshBigImgFO.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(2))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
      } yield {
        (bigimg5 ++: peopleRecommendBigImg ++: peopleRecommend ++: moderRecommend ++: bigimg).take(1) ++: (hotWords ++: hots).take((count / 5).toInt) ++: (peopleRecommend ++: refreshByClick ++: moderRecommend ++: recommends).take(count.toInt) ++: commons
      }
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>
        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)
        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.map { n =>
          if (n.rtype.getOrElse(0) == 999) {
            n.copy(ptime = localDateTime.plusSeconds(-1))
          } else if (n.rtype.getOrElse(0) == 4) {
            n.copy(ptime = localDateTime.plusSeconds(-2))
          } else {
            n.copy(ptime = localDateTime.plusSeconds(Random.nextInt(3) - 10))
          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻
        seq.sortBy(_.ptime).filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
      }.map(_.take(20))

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      //专题
      result.map { seq => seq.filter(_.rtype.getOrElse(0) == 4).map { v => topicNewsReadDAO.insertByTid(uid, v.nid.toInt) } }
      //将99的改回
      result.map { seq =>
        seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 999)
            r.copy(rtype = Some(2))
          else
            r
        }.sortBy(_.ptime)
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.loadFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      //----热点部分----
      //有评论
      val refreshHotFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByHot((page - 1) * count, count / 5, newTimeCursor, uid)
      //百度热词
      val refreshHotWordFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByHotWord((page - 1) * count, count / 10, newTimeCursor, uid)

      //----推荐部分----
      //模型非人工推荐
      val refreshModerRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByModelRecommend((page - 1) * count, (count - count / 4) / 2, newTimeCursor, uid)
      //模型人工推荐
      val refreshByPeopleRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByPeopleRecommend((page - 1) * count, count / 4, newTimeCursor, uid)
      //人工推荐(没有推荐模型时,直接出人工推荐)
      val refreshRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, 2 * count / 4)
      //根据点击日志推荐
      val refreshByClickFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByClick((page - 1) * count, (count - count / 4) / 2 + 1, newTimeCursor, uid)

      //普通新闻(可能情况:其他都没有数据,全出普通)
      val refreshCommonFO: Future[Seq[NewsRow]] = newsRecommendDAO.refresh((page - 1) * count, count, newTimeCursor, uid)

      //----大图部分----
      //系统人工推荐大图
      val refreshByPeopleRecommendBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.refreshByPeopleRecommendBigImg(uid, 0, 1)
      //等级为5大图新闻
      val refreshBigImgFO5: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg5(uid, 0, 1)
      //等级为5新闻
      val refreshFO5: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid5(uid, 0, 2)

      //普通大图
      val refreshBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)

      //专题
      val topicsFO: Future[Seq[TopicList]] = t match {
        case 1 => topicListDAO.topicShow(uid).map(_.take(3))
        case _ => Future.successful { Seq[TopicList]() }
      }

      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) } //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频
        //将热词的rtype改为99,后面要根据热点新闻改时间,这里的热词会打乱时间顺序,最后再改回来
        hotWords <- refreshHotWordFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) }
        moderRecommend <- refreshModerRecommendFO.map {
          case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime)
        }
        refreshByClick <- refreshByClickFO.map {
          case newsRow: Seq[NewsRow] => newsRow.map { r =>
            NewsFeedResponse.from(r).copy(rtype = Some(0))
          }.sortBy(_.ptime)
        }
        peopleRecommend <- refreshByPeopleRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(2)) }.sortBy(_.ptime) }
        commons <- refreshCommonFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        recommends <- refreshRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(rtype = Some(2)) } }
        bigimg5 <- refreshBigImgFO5.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(999))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        level5 <- refreshFO5.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            NewsFeedResponse.from(r._1).copy(rtype = Some(999))
          }
        }
        peopleRecommendBigImg <- refreshByPeopleRecommendBigImgFO.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(2))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        bigimg <- refreshBigImgFO.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(2))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        topics <- topicsFO.map { case seq: Seq[TopicList] => seq.map { topic => NewsFeedResponse.from(topic) } }
      } yield {
        //大图:每次最多出一个,1、有5等级的出5等级,2、没5等级从模型荐选择大图,3、5等级和模型推荐大图都没有,看是不是冷启动(即有没有模型推荐数据,1和2都没大图,也没有模型推荐数据,直接出人工大图,有模型推荐数据,没大图,说明没有用户喜好的频道大图,不推荐大图)
        ((bigimg5 ++: peopleRecommendBigImg ++: peopleRecommend ++: moderRecommend ++: bigimg).take(1) ++: level5 ++: topics ++: (hotWords ++: hots).take((count / 5).toInt) ++: (peopleRecommend ++: refreshByClick ++: moderRecommend ++: recommends).take(count.toInt) ++: commons).take(25)
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
          } else {
            n.copy(ptime = newTimeCursor.plusSeconds(Random.nextInt(7)))
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
      }.map(_.take(20))

      //没有广告,只需排除专题
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      //专题
      result.map { seq => seq.filter(_.rtype.getOrElse(0) == 4).map { v => topicNewsReadDAO.insertByTid(uid, v.nid.toInt) } }
      //将99的改回
      result.map { seq =>
        seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 999)
            r.copy(rtype = Some(2))
          else
            r
        }.sortBy(_.ptime)
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.refreshFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedWithAd(uid: Long, page: Long, count: Long, timeCursor: Long, adbody: String, t: Int, remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByHot((page - 1) * count, count / 5, msecondsToDatetime(timeCursor), uid)
      val loadHotWordFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByHotWord((page - 1) * count, count / 10, msecondsToDatetime(timeCursor), uid)
      val loadModerRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByModelRecommend((page - 1) * count, (count - count / 4) / 2, msecondsToDatetime(timeCursor), uid)
      val loadByPeopleRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByPeopleRecommend((page - 1) * count, count / 4, msecondsToDatetime(timeCursor), uid)
      val loadCommonFO: Future[Seq[NewsRow]] = newsRecommendDAO.load((page - 1) * count, count, msecondsToDatetime(timeCursor), uid)
      val refreshRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, 2 * count / 4)
      val refreshByClickFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByClick((page - 1) * count, (count - count / 4) / 2 + 1, msecondsToDatetime(timeCursor), uid)

      val refreshByPeopleRecommendBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.refreshByPeopleRecommendBigImg(uid, 0, 1)
      val refreshBigImgFO5: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg5(uid, 0, 1)
      val refreshBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)

      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress)

      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- loadHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) }
        hotWords <- loadHotWordFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) }
        moderRecommend <- loadModerRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        refreshByClick <- refreshByClickFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        peopleRecommend <- loadByPeopleRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(2)) }.sortBy(_.ptime) }
        commons <- loadCommonFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        recommends <- refreshRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(rtype = Some(2)) } }
        bigimg5 <- refreshBigImgFO5.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(999))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        peopleRecommendBigImg <- refreshByPeopleRecommendBigImgFO.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(2))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        bigimg <- refreshBigImgFO.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(2))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        ad <- adFO
      } yield {
        ((bigimg5 ++: peopleRecommendBigImg ++: peopleRecommend ++: moderRecommend ++: bigimg).take(1) ++: ad ++: (hotWords ++: hots).take((count / 5).toInt) ++: (peopleRecommend ++: refreshByClick ++: moderRecommend ++: recommends).take(count.toInt) ++: commons).take(20)
      }
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>

        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)

        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.map { n =>
          if (n.rtype.getOrElse(0) == 999) {
            n.copy(ptime = localDateTime.plusSeconds(-1))
          } else if (n.rtype.getOrElse(0) == 4) {
            n.copy(ptime = localDateTime.plusSeconds(-2))
          } else {
            var newtime = localDateTime.plusSeconds(Random.nextInt(3) - 10)
            n.copy(ptime = newtime)
          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻
        seq.sortBy(_.ptime).filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
      }.map(_.take(20))

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      //专题
      result.map { seq => seq.filter(_.rtype.getOrElse(0) == 4).map { v => topicNewsReadDAO.insertByTid(uid, v.nid.toInt) } }
      //将99的改回
      result.map { seq =>
        seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 999)
            r.copy(rtype = Some(2))
          else
            r
        }.sortBy(_.ptime)
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.loadFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  //加上广告
  def refreshFeedWithAd(uid: Long, page: Long, count: Long, timeCursor: Long, adbody: String, t: Int, remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      //----热点部分----
      //有评论
      val refreshHotFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByHot((page - 1) * count, count / 5, newTimeCursor, uid)
      //百度热词
      val refreshHotWordFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByHotWord((page - 1) * count, count / 10, newTimeCursor, uid)

      //----推荐部分----
      //模型非人工推荐
      val refreshModerRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByModelRecommend((page - 1) * count, (count - count / 4) / 2, newTimeCursor, uid)
      //模型人工推荐
      val refreshByPeopleRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByPeopleRecommend((page - 1) * count, count / 4, newTimeCursor, uid)
      //人工推荐(没有推荐模型时,直接出人工推荐)
      val refreshRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, 2 * count / 4)
      //根据点击日志推荐
      val refreshByClickFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByClick((page - 1) * count, (count - count / 4) / 2 + 1, newTimeCursor, uid)

      //普通新闻(可能情况:其他都没有数据,全出普通)
      val refreshCommonFO: Future[Seq[NewsRow]] = newsRecommendDAO.refresh((page - 1) * count, count, newTimeCursor, uid)

      //----大图部分----
      //系统人工推荐大图
      val refreshByPeopleRecommendBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.refreshByPeopleRecommendBigImg(uid, 0, 1)
      //等级为5大图新闻
      val refreshBigImgFO5: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg5(uid, 0, 1)
      //等级为5新闻
      val refreshFO5: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid5(uid, 0, 2)

      //普通大图
      val refreshBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)

      //专题
      val topicsFO: Future[Seq[TopicList]] = t match {
        case 1 => topicListDAO.topicShow(uid).map(_.take(3))
        case _ => Future.successful { Seq[TopicList]() }
      }

      val body: String = adbody
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(body, remoteAddress)

      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) } //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频
        //将热词的rtype改为99,后面要根据热点新闻改时间,这里的热词会打乱时间顺序,最后再改回来
        hotWords <- refreshHotWordFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) }
        moderRecommend <- refreshModerRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        refreshByClick <- refreshByClickFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        peopleRecommend <- refreshByPeopleRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(2)) }.sortBy(_.ptime) }
        commons <- refreshCommonFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        recommends <- refreshRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(rtype = Some(2)) } }
        bigimg5 <- refreshBigImgFO5.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(999))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        level5 <- refreshFO5.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            NewsFeedResponse.from(r._1).copy(rtype = Some(999))
          }
        }
        peopleRecommendBigImg <- refreshByPeopleRecommendBigImgFO.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(2))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        bigimg <- refreshBigImgFO.map {
          case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r =>
            val feed = NewsFeedResponse.from(r._1).copy(rtype = Some(2))
            if (r._2.bigimg.getOrElse(0) > 0) {
              feed.copy(style = 10 + r._2.bigimg.getOrElse(1))
            } else {
              feed
            }
          }
        }
        topics <- topicsFO.map { case seq: Seq[TopicList] => seq.map { topic => NewsFeedResponse.from(topic) } }
        ad <- adFO
      } yield {
        ((bigimg5 ++: peopleRecommendBigImg ++: peopleRecommend ++: moderRecommend ++: bigimg).take(1) ++: level5 ++: topics ++: ad ++: (hotWords ++: hots).take((count / 5).toInt) ++: (peopleRecommend ++: refreshByClick ++: moderRecommend ++: recommends).take(count.toInt) ++: commons).take(20)
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
          } else {
            n.copy(ptime = newTimeCursor.plusSeconds(Random.nextInt(7)))
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
      }.map(_.take(20))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      //专题
      result.map { seq => seq.filter(_.rtype.getOrElse(0) == 4).map { v => topicNewsReadDAO.insertByTid(uid, v.nid.toInt) } }
      //将99的改回
      result.map { seq =>
        seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 999)
            r.copy(rtype = Some(2))
          else
            r
        }.sortBy(_.ptime)
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.refreshFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def listPublisherWithFlag(uid: Option[Long], keywords: String): Future[Seq[(NewsPublisherRow, Long)]] = {
    newsRecommendDAO.listPublisherWithFlag(uid, keywords).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.listPublisherWithFlag($uid, $keywords): ${e.getMessage}")
        Seq[(NewsPublisherRow, Long)]()
    }
  }

}