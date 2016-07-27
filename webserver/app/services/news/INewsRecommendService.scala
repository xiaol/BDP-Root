package services.news

import java.util
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.{ NewsRecommendResponse, _ }
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import dao.news.{ NewsDAO, NewsRecommendDAO, NewsRecommendReadDAO }
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangshl on 16/7/15.
 */
@ImplementedBy(classOf[NewsRecommendService])
trait INewsRecommendService {
  def operate(nid: Long, method: String, level: Option[Double], bigimg: Option[Int]): Future[Option[Long]]
  def insert(newsRecommend: NewsRecommend): Future[Option[Long]]
  def delete(nid: Long): Future[Option[Long]]
  def listNewsAndCountByRecommand(channel: Long, ifrecommend: Int, page: Long, count: Long): Future[(Seq[NewsRecommendResponse], Long)]
  def listNewsByRecommand(channel: Long, ifrecommend: Int, page: Long, count: Long): Future[Seq[NewsRecommendResponse]]
  def listNewsByRecommandCount(channel: Long, ifrecommend: Int): Future[Int]
  def listNewsBySearch(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int): Future[(Seq[NewsRecommendResponse], Long)]
  def loadFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]]
  def refreshFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]]
  def listPublisherWithFlag(uid: Option[Long], keywords: String): Future[Seq[(NewsPublisherRow, Long)]]
}

class NewsRecommendService @Inject() (val newsRecommendDAO: NewsRecommendDAO, val newsEsService: NewsEsService,
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

  def listNewsAndCountByRecommand(channel: Long, ifrecommend: Int, page: Long, count: Long): Future[(Seq[NewsRecommendResponse], Long)] = {
    val newsRecommendResponses: Future[Seq[NewsRecommendResponse]] = listNewsByRecommand(channel, ifrecommend, page, count)
    val count1: Future[Int] = listNewsByRecommandCount(channel: Long, ifrecommend: Int)
    for {
      n <- newsRecommendResponses
      c <- count1
    } yield (n, c.toLong)
  }

  def listNewsByRecommand(channel: Long, ifrecommend: Int, page: Long, count: Long): Future[Seq[NewsRecommendResponse]] = {
    newsRecommendDAO.listNewsByRecommand(channel, ifrecommend, (page - 1) * count, count).map {
      case pairs: Seq[NewsRecommendResponse] => pairs.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.listNewsByRecommand($channel, $ifrecommend): ${e.getMessage}")
        Seq[NewsRecommendResponse]()
    }
  }

  def listNewsByRecommandCount(channel: Long, ifrecommend: Int): Future[Int] = {
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
      val loadHotFO: Future[Seq[NewsRow]] = newsDAO.loadByHot((page - 1) * count, count / 2, msecondsToDatetime(timeCursor))
      val lostColdFO: Future[Seq[NewsRow]] = newsDAO.loadByCold((page - 1) * count, count / 2, msecondsToDatetime(timeCursor))
      //人工推荐新闻,每个推荐等级依次一条条显示
      val loadRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, count)
      //取一条大图新闻,作为头条
      val loadBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)
      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- loadHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        colds <- lostColdFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        recommends <- loadRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1) } }
        bigimg <- loadBigImgFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(style = 10 + r._2.bigimg.getOrElse(1)) } }
      } yield {
        bigimg ++: recommends ++: hots ++: colds
      }
      val result: Future[Seq[NewsFeedResponse]] = r.map(_.take(20))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      result
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.loadFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
      val refreshHotFO: Future[Seq[NewsRow]] = newsDAO.refreshByHot((page - 1) * count, count / 2, newTimeCursor)
      val refreshColdFO: Future[Seq[NewsRow]] = newsDAO.refreshByCold((page - 1) * count, count / 2, newTimeCursor)
      val refreshRecommendFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, count)
      val refreshBigImgFO: Future[Seq[(NewsRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)
      val r: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        colds <- refreshColdFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        recommends <- refreshRecommendFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1) } }
        bigimg <- refreshBigImgFO.map { case newsRow: Seq[(NewsRow, NewsRecommend)] => newsRow.map { r => NewsFeedResponse.from(r._1).copy(style = 10 + r._2.bigimg.getOrElse(1)) } }
      } yield {
        bigimg ++: recommends ++: hots ++: colds
      }
      val result: Future[Seq[NewsFeedResponse]] = r.map(_.take(20))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      result
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