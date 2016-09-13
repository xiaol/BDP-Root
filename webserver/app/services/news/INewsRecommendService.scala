package services.news

import java.util
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.{ NewsRecommendResponse, _ }
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import dao.news.{ NewsDAO, NewsRecommendDAO, NewsRecommendReadDAO }
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
  def loadFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]]
  def refreshFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]]
  def listPublisherWithFlag(uid: Option[Long], keywords: String): Future[Seq[(NewsPublisherRow, Long)]]
}

class NewsRecommendService @Inject() (val newsRecommendDAO: NewsRecommendDAO, val newsEsService: NewsEsService, val adResponseService: AdResponseService,
                                      val newsDAO: NewsDAO, val newsRecommendReadDAO: NewsRecommendReadDAO) extends INewsRecommendService {

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
    val newsRecommendResponses: Future[Seq[NewsRecommendResponse]] = listNewsByRecommand(channel, ifrecommend, page, count)
    val count1: Future[Int] = listNewsByRecommandCount(channel: Option[Long], ifrecommend: Int)
    for {
      n <- newsRecommendResponses
      c <- count1
    } yield (n, c.toLong)
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
      newsRecommendResponses <- listNewsBySearch(newsFeedResponses._1: Seq[NewsFeedResponse], newsRecommends: Seq[NewsRecommend])
    } yield (newsRecommendResponses, newsFeedResponses._2)
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

  def loadFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByHot((page - 1) * count, count / 5, msecondsToDatetime(timeCursor), uid)
      val loadHotWordFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByHotWord((page - 1) * count, count / 10, msecondsToDatetime(timeCursor), uid)
      //      val loadModerRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByModelRecommend((page - 1) * count, count / 5, msecondsToDatetime(timeCursor), uid)
      val loadCommonFO: Future[Seq[NewsRow]] = newsRecommendDAO.load((page - 1) * count, count - (count / 5), msecondsToDatetime(timeCursor))
      //人工推荐新闻,每个推荐等级依次一条条显示
      val loadRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, count / 5)
      //取一条大图新闻,作为头条
      val loadBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)
      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- loadHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) }
        hotWords <- loadHotWordFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(99)) }.sortBy(_.ptime) }
        //        moderRecommend <- loadModerRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        commons <- loadCommonFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        recommends <- loadRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(rtype = Some(2)) } }
        bigimg <- loadBigImgFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(style = 10 + r._2.bigimg.getOrElse(1)).copy(rtype = Some(2)) } }
      } yield {
        bigimg ++: recommends ++: (hotWords ++: hots).take((count / 5).toInt) ++: commons //++: moderRecommend ++: commons
      }
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>
        //获取热点最早一条新闻时间
        var timehotlast: Option[LocalDateTime] = None
        var nidlast: Option[Long] = None
        seq.foreach { n =>
          if (n.rtype.getOrElse(0) == 1) {
            timehotlast = Some(n.ptime)
            nidlast = Some(n.nid)
          }
        }
        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)

        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.map { n =>
          if (nidlast.getOrElse(0) != n.nid) {
            var newtime = timehotlast.getOrElse(localDateTime).plusSeconds(Random.nextInt(6) - 3)
            if (newtime.isBefore(localDateTime))
              newtime = localDateTime.plusSeconds(-1)
            n.copy(ptime = newtime)
          } else {
            n
          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻
        seq.filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
      }.map(_.take(20))

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      result.map { seq =>
        seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 99)
            r.copy(rtype = Some(1))
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

  def refreshFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      val refreshHotFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByHot((page - 1) * count, count / 5, newTimeCursor, uid)
      val refreshHotWordFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByHotWord((page - 1) * count, count / 10, newTimeCursor, uid)
      //      val refreshModerRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByModelRecommend((page - 1) * count, count / 5, newTimeCursor, uid)
      val refreshCommonFO: Future[Seq[NewsRow]] = newsRecommendDAO.refresh((page - 1) * count, count - (count / 5), newTimeCursor, uid)
      val refreshRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, count / 5)
      val refreshBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)

      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) } //rtype推荐类型:0普通、1热点、2推送
        //将热词的rtype改为99,后面要根据热点新闻改时间,这里的热词会打乱时间顺序,最后再改回来
        hotWords <- refreshHotWordFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(99)) }.sortBy(_.ptime) }
        //        moderRecommend <- refreshModerRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        commons <- refreshCommonFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        recommends <- refreshRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(rtype = Some(2)) } }
        bigimg <- refreshBigImgFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(style = 10 + r._2.bigimg.getOrElse(1)).copy(rtype = Some(2)) } }
      } yield {
        bigimg ++: recommends ++: (hotWords ++: hots).take((count / 5).toInt) ++: commons //++: moderRecommend ++: commons
      }
      //规则一:去重复新闻,一个来源可能重复
      //规则二:重做时间
      //规则三:过滤连续重复来源
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>
        //获取热点最新一条新闻时间
        var timehotfirst: Option[LocalDateTime] = None
        var nidfirst: Option[Long] = None
        var counthot = 0
        seq.foreach { n =>
          if (counthot == 0 && n.rtype.getOrElse(0) == 1) {
            counthot = counthot + 1
            timehotfirst = Some(n.ptime)
            nidfirst = Some(n.nid)
          }
        }
        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)

        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.map { n =>
          if (nidfirst.getOrElse(0) != n.nid) {
            var newtime = timehotfirst.getOrElse(localDateTime).plusSeconds(Random.nextInt(6) - 3)
            if (newtime.isBefore(localDateTime))
              newtime = localDateTime.plusSeconds(1)
            n.copy(ptime = newtime)
          } else {
            n
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
      }.map(_.take(20))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      //将99的改回
      result.map { seq =>
        seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 99)
            r.copy(rtype = Some(1))
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

  def loadFeedWithAd(uid: Long, page: Long, count: Long, timeCursor: Long, adbody: String): Future[Seq[NewsFeedResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByHot((page - 1) * count, count / 5, msecondsToDatetime(timeCursor), uid)
      val loadHotWordFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByHotWord((page - 1) * count, count / 10, msecondsToDatetime(timeCursor), uid)
      //      val loadModerRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.loadByModelRecommend((page - 1) * count, count / 5, msecondsToDatetime(timeCursor), uid)
      val loadCommonFO: Future[Seq[NewsRow]] = newsRecommendDAO.load((page - 1) * count, count - (count / 5), msecondsToDatetime(timeCursor))
      //人工推荐新闻,每个推荐等级依次一条条显示
      val loadRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, count / 5)
      //取一条大图新闻,作为头条
      val loadBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)

      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody)

      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- loadHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) }
        hotWords <- loadHotWordFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(99)) }.sortBy(_.ptime) }
        //        moderRecommend <- loadModerRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        commons <- loadCommonFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        recommends <- loadRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(rtype = Some(2)) } }
        bigimg <- loadBigImgFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(style = 10 + r._2.bigimg.getOrElse(1)).copy(rtype = Some(2)) } }
        ad <- adFO
      } yield {
        bigimg ++: recommends ++: (hotWords ++: hots).take((count / 5).toInt) ++: ad ++: commons //++: moderRecommend ++: commons
      }
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>
        //获取热点最早一条新闻时间
        var timehotlast: Option[LocalDateTime] = None
        var nidlast: Option[Long] = None
        seq.foreach { n =>
          if (n.rtype.getOrElse(0) == 1) {
            timehotlast = Some(n.ptime)
            nidlast = Some(n.nid)
          }
        }
        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)

        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.map { n =>
          if (nidlast.getOrElse(0) != n.nid) {
            var newtime = timehotlast.getOrElse(localDateTime).plusSeconds(Random.nextInt(6) - 3)
            if (newtime.isBefore(localDateTime))
              newtime = localDateTime.plusSeconds(-1)
            n.copy(ptime = newtime)
          } else {
            n
          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻
        seq.filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
      }.map(_.take(20))

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      result.map { seq =>
        seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 99)
            r.copy(rtype = Some(1))
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
  def refreshFeedWithAd(uid: Long, page: Long, count: Long, timeCursor: Long, adbody: String): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      val refreshHotFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByHot((page - 1) * count, count / 5, newTimeCursor, uid)
      val refreshHotWordFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByHotWord((page - 1) * count, count / 10, newTimeCursor, uid)
      //      val refreshModerRecommendFO: Future[Seq[NewsRow]] = newsRecommendDAO.refreshByModelRecommend((page - 1) * count, count / 5, newTimeCursor, uid)
      val refreshCommonFO: Future[Seq[NewsRow]] = newsRecommendDAO.refresh((page - 1) * count, count - (count / 5), newTimeCursor, uid)
      val refreshRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, count / 5)
      val refreshBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)

      val body: String = adbody
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(body)

      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(1)) }.sortBy(_.ptime) } //rtype推荐类型:0普通、1热点、2推送
        //将热词的rtype改为99,后面要根据热点新闻改时间,这里的热词会打乱时间顺序,最后再改回来
        hotWords <- refreshHotWordFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(99)) }.sortBy(_.ptime) }
        //        moderRecommend <- refreshModerRecommendFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        commons <- refreshCommonFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r).copy(rtype = Some(0)) }.sortBy(_.ptime) }
        recommends <- refreshRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(rtype = Some(2)) } }
        bigimg <- refreshBigImgFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(style = 10 + r._2.bigimg.getOrElse(1)).copy(rtype = Some(2)) } }
        ad <- adFO
      } yield {
        bigimg ++: recommends ++: (hotWords ++: hots).take((count / 5).toInt) ++: ad ++: commons //++: moderRecommend ++: commons
      }
      //规则一:去重复新闻,一个来源可能重复
      //规则二:重做时间
      //规则三:过滤连续重复来源
      val result: Future[Seq[NewsFeedResponse]] = r.map { seq =>
        //获取热点最新一条新闻时间
        var timehotfirst: Option[LocalDateTime] = None
        var nidfirst: Option[Long] = None
        var counthot = 0
        seq.foreach { n =>
          if (counthot == 0 && n.rtype.getOrElse(0) == 1) {
            counthot = counthot + 1
            timehotfirst = Some(n.ptime)
            nidfirst = Some(n.nid)
          }
        }
        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)

        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.map { n =>
          if (nidfirst.getOrElse(0) != n.nid) {
            var newtime = timehotfirst.getOrElse(localDateTime).plusSeconds(Random.nextInt(6) - 3)
            if (newtime.isBefore(localDateTime))
              newtime = localDateTime.plusSeconds(1)
            n.copy(ptime = newtime)
          } else {
            n
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
      }.map(_.take(20))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      //将99的改回
      result.map { seq =>
        seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 99)
            r.copy(rtype = Some(1))
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