package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import dao.news.PvDetailDAO
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

/**
 * Created by zhangsl on 2016-12-02.
 *
 */

@ImplementedBy(classOf[TopicService])
trait IPvdetailService {
  def insert(pvDetail: PvDetail)
}

class PvdetailService @Inject() (val pvDetailDAO: PvDetailDAO) extends IPvdetailService {

  def insert(pvDetail: PvDetail) = {
    pvDetailDAO.insert(pvDetail).recover {
      case NonFatal(e) =>
        Logger.error(s"Within PvdetailService.insert($pvDetail): ${e.getMessage}")
        None
    }
  }

}