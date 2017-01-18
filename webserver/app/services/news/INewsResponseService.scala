package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import dao.news._
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import commons.models.news.SimpleNewsRow

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[NewsResponseService])
trait INewsResponseService {
  def news(): Future[Seq[SimpleNewsRow]]
}

class NewsResponseService @Inject() (val newsDAO: NewsResponseDao) extends INewsResponseService {

  def news(): Future[Seq[SimpleNewsRow]] = {
    {
      val reuslt: Future[Seq[SimpleNewsRow]] = newsDAO.news().map { seq =>
        seq.map { news =>
          val imgs = news._3 match {
            case Some(str) => Some(str.substring(1, str.length - 1).substring(0, str.length - 2).split(",").toList)
            case _         => None
          }
          SimpleNewsRow(news._1, news._2, imgs)
        }
      }
      reuslt
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsResponseService.news(): ${e.getMessage}")
        Seq[SimpleNewsRow]()
    }
  }

}
