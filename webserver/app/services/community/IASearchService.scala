package services.community

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.AdResponse
import commons.models.community.{ ASearchResponse, ASearchRow }
import dao.community.ASearchDAO
import org.joda.time.LocalDateTime
import play.api.Logger
import services.advertisement.AdResponseService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-21.
 *
 */

@ImplementedBy(classOf[ASearchService])
trait IASearchService {
  def listByRefer(refer: String, page: Long, count: Long): Future[Seq[ASearchRow]]
  def listByReferWithAd(refer: String, page: Long, count: Long, adbody: Option[String], remoteAddress: Option[String]): Future[Seq[ASearchResponse]]
  def insertMulti(searchItemRows: Seq[ASearchRow]): Future[Seq[Long]]
}

class ASearchService @Inject() (val asearchDAO: ASearchDAO, val adResponseService: AdResponseService) extends IASearchService {

  def listByRefer(refer: String, page: Long, count: Long): Future[Seq[ASearchRow]] = {
    asearchDAO.listByRefer(refer, (page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within ASearchService.listByRefer($refer, $page, $count): ${e.getMessage}")
        Seq[ASearchRow]()
    }
  }

  def listByReferWithAd(refer: String, page: Long, count: Long, adbody: Option[String], remoteAddress: Option[String]): Future[Seq[ASearchResponse]] = {
    {
      val result = asearchDAO.listByRefer(refer, (page - 1) * count, count)
      val adFO: Future[Option[AdResponse]] = adResponseService.getAdResponse(adbody.get, remoteAddress)

      val response = for {
        r <- result.map { seq =>
          seq.map { aSearchRow =>
            ASearchResponse.from(aSearchRow.asearch, None)
          }
        }
        ad <- adFO.map { adResponseOpt =>
          adResponseOpt match {
            case Some(adResponse) => Seq[ASearchResponse](ASearchResponse("", "", "", 0, "", LocalDateTime.now(), None, None, None, None, Some(3), Some(3), None, Some(adResponse)))
            case None             => Seq[ASearchResponse]()
          }
        }
      } yield {
        r ++: ad
      }

      response.map { seq =>
        var flag = true
        //若只有广告,返回空
        if (seq.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          seq
        } else {
          Seq[ASearchResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ASearchService.listByRefer($refer, $page, $count): ${e.getMessage}")
        Seq[ASearchResponse]()
    }
  }

  def insertMulti(searchItemRows: Seq[ASearchRow]): Future[Seq[Long]] = {
    asearchDAO.insertAll(searchItemRows).recover {
      case NonFatal(e) =>
        Logger.error(s"Within ASearchService.insertMulti(row.size: ${searchItemRows.size}, refer: ${searchItemRows.map(_.refer).toSet}): ${e.getMessage}")
        Seq[Long]()
    }
  }
}
