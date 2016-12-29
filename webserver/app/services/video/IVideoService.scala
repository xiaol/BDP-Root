package services.video

import java.util
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.{ NewsRecommendResponse, _ }
import commons.models.video.VideoRow
import commons.utils.JodaOderingImplicits
import commons.utils.JodaUtils._
import dao.news._
import dao.userprofiles.HateNewsDAO
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
}

class VideoService @Inject() (val videoDAO: VideoDAO, val adResponseService: AdResponseService) extends IVideoService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def refreshFeedWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor) //createTimeCursor4Refresh(timeCursor)

      val result: Future[Seq[VideoRow]] = videoDAO.refreshVideoByChannel(chid, (page - 1) * count, count, newTimeCursor, nid)
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress, uid)

      val response = for {
        r <- result.map { case newsRows: Seq[VideoRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        ad <- adFO
      } yield {
        r ++: ad
      }

      response.map { seq =>
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          //将广告时间随机成任意一条新闻时间
          seq.map { r =>
            if (r.rtype.getOrElse(0) == 3)
              r.copy(ptime = seq(Random.nextInt(seq.length - 1)).ptime)
            else
              r
          }.sortBy(_.ptime)
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within VideoService.refreshFeedByChannelWithAd($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadFeedWithAd(uid: Long, chid: Long, sechidOpt: Option[Long], page: Long, count: Long, timeCursor: Long, adbody: String, remoteAddress: Option[String], nid: Option[Long]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor) //createTimeCursor4Refresh(timeCursor)

      val result: Future[Seq[VideoRow]] = videoDAO.loadVideoByChannel(chid, (page - 1) * count, count, newTimeCursor, nid)
      val adFO: Future[Seq[NewsFeedResponse]] = adResponseService.getAdResponse(adbody, remoteAddress, uid)

      val response = for {
        r <- result.map { case newsRows: Seq[VideoRow] => newsRows.map { r => NewsFeedResponse.from(r) }.sortBy(_.ptime) }
        ad <- adFO
      } yield {
        r ++: ad
      }

      response.map { seq =>
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          //将广告时间随机成任意一条新闻时间
          seq.map { r =>
            if (r.rtype.getOrElse(0) == 3)
              r.copy(ptime = seq(Random.nextInt(seq.length - 1)).ptime)
            else
              r
          }.sortBy(_.ptime)
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within VideoService.refreshFeedByChannelWithAd($chid, $sechidOpt, $timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    val nowTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(0))
    if (reqTimeCursor.isBefore(oldTimeCursor) || reqTimeCursor.isAfter(nowTimeCursor)) oldTimeCursor else reqTimeCursor
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
        Logger.error(s"Within NewsService.findDetailsWithProfileByNid($nid, $uidOpt): ${e.getMessage}")
        None
    }
  }

}