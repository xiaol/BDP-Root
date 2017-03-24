package services.news

import java.util
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.{ NewsRecommendResponse, _ }
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
  def listPublisherWithFlag(uid: Option[Long], keywords: String): Future[Seq[(NewsPublisherRow, Long)]]
}

class NewsRecommendService @Inject() (val newsRecommendDAO: NewsRecommendDAO, val newsEsService: NewsEsService, val adResponseService: AdResponseService,
                                      val newsrecommendclickDAO: NewsrecommendclickDAO,
                                      val topicListDAO: TopicListDAO, val topicNewsReadDAO: TopicNewsReadDAO, val hateNewsDAO: HateNewsDAO,
                                      val newsFeedDao: NewsFeedDao) extends INewsRecommendService {

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

  def listPublisherWithFlag(uid: Option[Long], keywords: String): Future[Seq[(NewsPublisherRow, Long)]] = {
    newsRecommendDAO.listPublisherWithFlag(uid, keywords).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsRecommendService.listPublisherWithFlag($uid, $keywords): ${e.getMessage}")
        Seq[(NewsPublisherRow, Long)]()
    }
  }

}