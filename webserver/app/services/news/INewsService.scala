package services.news

import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import dao.kmeans.NewsKmeansDAO
import dao.news.{ NewsPublisherDAO, NewsDAO, NewsRecommendDAO, NewsRecommendReadDAO }
import commons.models.news._
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import org.joda.time.LocalDateTime
import play.api.Logger
import play.api.libs.json.Json
import services.advertisement.AdResponseService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[NewsService])
trait INewsService {
  def findDetailsByNid(nid: Long): Future[Option[NewsDetailsResponse]]
  def loadFeedByChannel(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def refreshFeedByChannel(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def loadFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def refreshFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]]
  def insert(newsRow: NewsRow): Future[Option[Long]]
  def delete(nid: Long): Future[Option[Long]]
  def updateCollect(nid: Long, collect: Int): Future[Option[Int]]
  def updateConcern(nid: Long, concern: Int): Future[Option[Int]]
  def updateComment(docid: String, comment: Int): Future[Option[Int]]
}

class NewsService @Inject() (val newsDAO: NewsDAO, val newsRecommendDAO: NewsRecommendDAO, val newsRecommendReadDAO: NewsRecommendReadDAO,
                             val adResponseService: AdResponseService, val newsPublisherDAO: NewsPublisherDAO, val newsKmeansDAO: NewsKmeansDAO) extends INewsService with NewsCacheService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def findDetailsByNid(nid: Long): Future[Option[NewsDetailsResponse]] = {
    newsDAO.findByNid(nid).map {
      case Some(newsRow) => Some(NewsDetailsResponse.from(newsRow))
      case _             => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findDetailsByUrl($nid): ${e.getMessage}")
        None
    }
  }

  def findDetailsWithProfileByNid(nid: Long, uidOpt: Option[Long]): Future[Option[NewsDetailsResponse]] = {
    val result = uidOpt match {
      case None => getNewsRowCache(nid).flatMap {
        case Some(row) =>
          Logger.info("拿到缓存数据=============" + row.base.title)
          Future.successful(Some(NewsDetailsResponse.from(row)))
        case _ =>
          Logger.info("未拿到缓存数据=============" + nid)
          newsDAO.findByNid(nid).map {
            case Some(row) =>
              //存入缓存
              setNewsRowCache(row)
              Some(NewsDetailsResponse.from(row))
            case _ => None
          }
      }
      //有用户id情况,需要查询此用户对本条新闻的关心等情况,每个用户都不一样,用缓存作用不大
      case Some(uid) => newsDAO.findByNidWithProfile(nid, uid).map {
        case Some((row, c1, c2, c3)) => Some(NewsDetailsResponse.from(row, Some(c1), Some(c2), Some(c3)))
        case _                       => None
      }
    }

    //    val result: Future[Option[NewsDetailsResponse]] = uidOpt match {
    //      case None => newsDAO.findByNid(nid).map {
    //        case Some(row) =>
    //          var detail = NewsDetailsResponse.from(row)
    //          detail.content.\\("imag")
    //          Some(NewsDetailsResponse.from(row))
    //        case _ => None
    //      }
    //      case Some(uid) => newsDAO.findByNidWithProfile(nid, uid).map {
    //        case Some((row, c1, c2, c3)) => Some(NewsDetailsResponse.from(row, Some(c1), Some(c2), Some(c3)))
    //        case _                       => None
    //      }
    //    }
    result.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findDetailsWithProfileByNid($nid, $uidOpt): ${e.getMessage}")
        None
    }
  }

  def findNextDetailsWithProfileByNid(nid: Long, uidOpt: Option[Long], chid: Long): Future[Option[NewsDetailsResponse]] = {
    val result: Future[Option[NewsDetailsResponse]] = uidOpt match {
      case None => newsDAO.findNextByNid(nid, chid: Long).map {
        case Some(row) =>
          var detail = NewsDetailsResponse.from(row)
          detail.content.\\("imag")
          Some(NewsDetailsResponse.from(row))
        case _ => None
      }
      case Some(uid) => newsDAO.findNextByNidWithProfile(nid, uid, chid).map {
        case Some((row, c1, c2, c3)) => Some(NewsDetailsResponse.from(row, Some(c1), Some(c2), Some(c3)))
        case _                       => None
      }
    }
    result.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findNextDetailsWithProfileByNid($nid, $uidOpt): ${e.getMessage}")
        None
    }
  }

  def findLastDetailsWithProfileByNid(nid: Long, uidOpt: Option[Long], chid: Long): Future[Option[NewsDetailsResponse]] = {
    val result: Future[Option[NewsDetailsResponse]] = uidOpt match {
      case None => newsDAO.findLastByNid(nid, chid: Long).map {
        case Some(row) =>
          var detail = NewsDetailsResponse.from(row)
          detail.content.\\("imag")
          Some(NewsDetailsResponse.from(row))
        case _ => None
      }
      case Some(uid) => newsDAO.findLastByNidWithProfile(nid, uid, chid).map {
        case Some((row, c1, c2, c3)) => Some(NewsDetailsResponse.from(row, Some(c1), Some(c2), Some(c3)))
        case _                       => None
      }
    }
    result.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.findLastDetailsWithProfileByNid($nid, $uidOpt): ${e.getMessage}")
        None
    }
  }

  def loadFeedByRecommends(page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsDAO.loadByHot((page - 1) * count, count / 2, msecondsToDatetime(timeCursor), nid)
      val lostColdFO: Future[Seq[NewsRow]] = newsDAO.loadByCold((page - 1) * count, count / 2, msecondsToDatetime(timeCursor), nid)
      for {
        hots <- loadHotFO
        colds <- lostColdFO
      } yield {
        hots ++: colds match {
          case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByRecommends($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByRecommendsNew(uid: Long, page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsRecommendResponse]] = {
    {
      val loadHotFO: Future[Seq[NewsRow]] = newsDAO.loadByHot((page - 1) * count, count / 2, msecondsToDatetime(timeCursor), nid)
      val lostColdFO: Future[Seq[NewsRow]] = newsDAO.loadByCold((page - 1) * count, count / 2, msecondsToDatetime(timeCursor), nid)
      //人工推荐新闻,每个推荐等级依次一条条显示
      val loadRecommendFO: Future[Seq[(NewsSimpleRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUid(uid, 0, count)
      //取一条大图新闻,作为头条
      val loadBigImgFO: Future[Seq[(NewsSimpleRow, NewsRecommend)]] = newsRecommendDAO.listNewsByRecommandUidBigImg(uid, 0, 1)
      val r: Future[Seq[NewsRecommendResponse]] = for {
        hots <- loadHotFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsRecommendResponse.from(NewsFeedResponse.from(r)) }.sortBy(_.ptime) }
        colds <- lostColdFO.map { case newsRow: Seq[NewsRow] => newsRow.map { r => NewsRecommendResponse.from(NewsFeedResponse.from(r)) }.sortBy(_.ptime) }
        recommends <- loadRecommendFO.map { case newsRow: Seq[(NewsSimpleRow, NewsRecommend)] => newsRow.map { r => NewsRecommendResponse.from(NewsFeedResponse.from(r._1), r._2) } }
        bigimg <- loadBigImgFO.map { case newsRow: Seq[(NewsSimpleRow, NewsRecommend)] => newsRow.map { r => NewsRecommendResponse.from(NewsFeedResponse.from(r._1), r._2) } }
      } yield {
        bigimg ++: recommends ++: hots ++: colds
      }
      val result: Future[Seq[NewsRecommendResponse]] = r.map(_.take(20))
      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      result
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByRecommendsNew($timeCursor): ${e.getMessage}")
        Seq[NewsRecommendResponse]()
    }
  }

  def refreshFeedByRecommends(page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
      val refreshHotFO: Future[Seq[NewsRow]] = newsDAO.refreshByHot((page - 1) * count, count / 2, newTimeCursor, nid)
      val refreshColdFO: Future[Seq[NewsRow]] = newsDAO.refreshByCold((page - 1) * count, count / 2, newTimeCursor, nid)
      for {
        hots <- refreshHotFO
        colds <- refreshColdFO
      } yield {
        hots ++: colds match {
          case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByRecommends($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByChannel(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val result: Future[Seq[NewsRow]] = sechidOpt match {
      case Some(sechid) => newsDAO.loadBySeChannel(chid, sechid, (page - 1) * count, count, msecondsToDatetime(timeCursor), nid)
      case None         => newsDAO.loadByChannel(chid, (page - 1) * count, count, msecondsToDatetime(timeCursor), nid)
    }

    val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.base.nid.get, LocalDateTime.now()) } }
    //从结果中取要浏览的20条,插入已浏览表中
    newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }

    result.map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByChannel(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

    val result: Future[Seq[NewsRow]] = sechidOpt match {
      case Some(sechid) => newsDAO.refreshBySeChannel(chid, sechid, (page - 1) * count, count, newTimeCursor, nid)
      case None         => newsDAO.refreshByChannel(chid, (page - 1) * count, count, newTimeCursor, nid)
    }

    val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.map { v => NewsRecommendRead(uid, v.base.nid.get, LocalDateTime.now()) } }
    //从结果中取要浏览的20条,插入已浏览表中
    newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }

    result.map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByChannel($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedByChannelWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val result: Future[Seq[NewsRow]] = sechidOpt match {
        case Some(sechid) => newsDAO.queryBySeChannel(uid, chid, sechid, count)
        case None         => newsDAO.queryByChannel(uid, chid, count)
      }

      val resultKM: Future[Seq[NewsRow]] = newsKmeansDAO.queryByChannelWithKmeans(uid, chid, count / 2)

      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress, uid)

      val response = for {
        r <- result.map { case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        rkm <- resultKM.map { case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        ad <- adFO
      } yield {
        (rkm ++: ad ++: r).take(count.toInt)
      }

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }

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

  def refreshFeedByChannelWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
      val date = new Date(timeCursor)
      val localDateTime = LocalDateTime.fromDateFields(date)

      val result: Future[Seq[NewsRow]] = sechidOpt match {
        case Some(sechid) => newsDAO.queryBySeChannel(uid, chid, sechid, count)
        case None         => newsDAO.queryByChannel(uid, chid, count)
      }

      val resultKM: Future[Seq[NewsRow]] = newsKmeansDAO.queryByChannelWithKmeans(uid, chid, count / 2)

      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress, uid)

      val response = for {
        r <- result.map { case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        rkm <- resultKM.map { case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        ad <- adFO
      } yield {
        (rkm ++: ad ++: r).take(count.toInt)
      }

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = response.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now()) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }

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

  def loadFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    newsDAO.loadByLocation((page - 1) * count, count, msecondsToDatetime(timeCursor), province, city, district, nid).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedByLocation($timeCursor, $province, $city, $district): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedByLocation(page: Long, count: Long, timeCursor: Long, province: Option[String], city: Option[String], district: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
    newsDAO.refreshByLocation((page - 1) * count, count, newTimeCursor, province, city, district, nid).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedByLocation($timeCursor, $province, $city, $district): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedBySource(source: Long, page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    newsDAO.loadBySource(source, (page - 1) * count, count, msecondsToDatetime(timeCursor), nid).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.loadFeedBySource($source, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def refreshFeedBySource(source: Long, page: Long, count: Long, timeCursor: Long, nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)
    newsDAO.refreshBySource(source, (page - 1) * count, count, newTimeCursor, nid).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.refreshFeedBySource($source, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    val nowTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(0))
    if (reqTimeCursor.isBefore(oldTimeCursor) || reqTimeCursor.isAfter(nowTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def insert(newsRow: NewsRow): Future[Option[Long]] = {
    //    val result: Future[Option[Long]] = newsRow.base.pname match {
    //      case Some(pname) => newsPublisherDAO.findByName(pname).flatMap {
    //        case Some(newsPublisherRow: NewsPublisherRow) => newsDAO.insert(newsRow.copy(base = newsRow.base.copy(icon = newsPublisherRow.icon))).map { nid => Some(nid) }
    //        case _ => newsDAO.insert(newsRow).map { nid => Some(nid) }
    //      }
    //      case _ => newsDAO.insert(newsRow).map { nid => Some(nid) }
    //    }
    newsDAO.insert(newsRow).map { nid => Some(nid) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.insert(${newsRow.base.url}, ${newsRow.base.docid}, ${newsRow.base.title}): ${e.getMessage}")
        None
    }
  }

  def delete(nid: Long): Future[Option[Long]] = {
    newsDAO.delete(nid).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.delete($nid): ${e.getMessage}")
        None
    }
  }

  def updateCollect(nid: Long, collect: Int): Future[Option[Int]] = {
    newsDAO.findByNid(nid).flatMap {
      case Some(nr) =>
        val incr = nr.incr
        newsDAO.update(nid, nr.copy(incr = incr.copy(collect = incr.collect + collect))).map(_ => Some(incr.collect + collect))
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.updateCollect($nid, $collect): ${e.getMessage}")
        None
    }
  }

  def updateConcern(nid: Long, concern: Int): Future[Option[Int]] = {
    newsDAO.findByNid(nid).flatMap {
      case Some(nr) =>
        val incr = nr.incr
        newsDAO.update(nid, nr.copy(incr = incr.copy(concern = incr.concern + concern))).map(_ => Some(incr.concern + concern))
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.updateConcern($nid, $concern): ${e.getMessage}")
        None
    }
  }

  def updateComment(docid: String, comment: Int): Future[Option[Int]] = {
    newsDAO.findByDocid(docid).flatMap {
      case Some(nr) =>
        val incr = nr.incr
        newsDAO.update(nr.base.nid.get, nr.copy(incr = incr.copy(comment = incr.comment + comment))).map(_ => Some(incr.comment + comment))
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsService.updateComment($docid, $comment): ${e.getMessage}")
        None
    }
  }
}
