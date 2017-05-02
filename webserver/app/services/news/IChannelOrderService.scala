package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.channels._
import dao.news.ChannelOrderDAO
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangshl on 2017/5/2.
 */

@ImplementedBy(classOf[ChannelOrderService])
trait IChannelOrderService {
  def listByChannel(channel: Int): Future[Seq[ChannelOrderRow]]
}

class ChannelOrderService @Inject() (val channelOrderDAO: ChannelOrderDAO) extends IChannelOrderService {

  def listByChannel(channel: Int): Future[Seq[ChannelOrderRow]] = {
    channelOrderDAO.listByChannel(channel).recover {
      case NonFatal(e) =>
        Logger.error(s"Within ChannelOrderService.listByChannel($channel): ${e.getMessage}")
        Seq[ChannelOrderRow]()
    }
  }
}