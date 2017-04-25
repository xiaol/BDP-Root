package services.community

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.AdResponse
import commons.models.community.{ ASearchResponse, ASearchRow }
import commons.models.news.NewsFeedResponse
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
  def listByReferWithAd(refer: String, page: Long, count: Long, adbody: Option[String], remoteAddress: Option[String], ads: Int): Future[Seq[ASearchResponse]]
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

  def listByReferWithAd(refer: String, page: Long, count: Long, adbody: Option[String], remoteAddress: Option[String], ads: Int): Future[Seq[ASearchResponse]] = {
    {
      val result = asearchDAO.listByRefer(refer, (page - 1) * count, count)

      //广告,根据ads的类型来获取广告,猎鹰广告api:1 ,广点通sdk:2(服务端不需要返回任何广告) ,亦复广告api:3
      val adFO: Future[Seq[NewsFeedResponse]] = ads match {
        case 1 =>
          adbody match {
            case Some(body: String) => adResponseService.getAdNewsFeedResponse(body, remoteAddress)
            case _                  => Future.successful(Seq[NewsFeedResponse]())
          }
        case _ => Future.successful(Seq[NewsFeedResponse]())
      }

      val response = for {
        r <- result.map { seq =>
          seq.map { aSearchRow =>
            ASearchResponse.from(aSearchRow.asearch, None)
          }
        }
        ad <- adFO.map { adResponseOpt =>
          adResponseOpt.headOption match {
            case Some(newsFeedResponse) =>
              val img = newsFeedResponse.imgs match {
                case Some(imgs) => imgs.headOption
                case _          => None
              }
              Seq[ASearchResponse](ASearchResponse(newsFeedResponse.purl.getOrElse(""), newsFeedResponse.title, "", 0, newsFeedResponse.pname.getOrElse(""), LocalDateTime.now(), img, None, None, None, Some(3), Some(3), None, newsFeedResponse.adresponse))
            case None => Seq[ASearchResponse]()
          }
        }
      } yield {
        r.head.rtype.getOrElse(0) match {
          //相关视频将广告放第一个
          case 6 => ad ++: r
          case _ => r.take(3) ++: ad ++: r.drop(3)
        }
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
