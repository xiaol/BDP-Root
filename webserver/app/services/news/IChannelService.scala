package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.channels._
import dao.news.ChannelDAO
import dao.users.UserDAO

import scala.concurrent.Future
import scala.util.control.NonFatal
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[ChannelService])
trait IChannelService {
  def list(state: Int): Future[Seq[ChannelRow]]
  def listByUid(uid: Long): Future[Seq[ChannelRow]]
  def count(): Future[Option[Int]]
  def insert(channelRow: ChannelRow): Future[Option[Long]]
  def update(id: Long, channelRow: ChannelRow): Future[Option[ChannelRow]]
  def delete(id: Long): Future[Option[Long]]
}

class ChannelService @Inject() (val channelDAO: ChannelDAO, val userDAO: UserDAO) extends IChannelService {

  def list(state: Int = 1): Future[Seq[ChannelRow]] = {
    channelDAO.list(state).recover {
      case NonFatal(e) =>
        Logger.error(s"Within ChannelService.list($state): ${e.getMessage}")
        Seq[ChannelRow]()
    }
  }

  def listWithSeChannel(state: Int, sech: Int): Future[Seq[ChannelResponse]] = {
    val result: Future[Seq[ChannelResponse]] = sech match {
      case 0 => channelDAO.list(state).map { case chs => chs.map(ChannelResponse.from(_)) }
      case _ => channelDAO.listWithSeChannel(state)
        .map { case chPairs: Seq[(ChannelRow, Option[Seq[SeChannelRow]])] => chPairs.map { case (ch, schs) => ChannelResponse.from(ch, schs) } }
    }
    result.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ChannelService.listWithSeChannel($state, $sech): ${e.getMessage}")
        Seq[ChannelResponse]()
    }
  }

  def listByUid(uid: Long): Future[Seq[ChannelRow]] = {
    userDAO.getChannel(uid).flatMap {
      case Some(chs) => channelDAO.listByNames(chs).flatMap {
        case chrs: Seq[ChannelRow] if chrs.nonEmpty => Future.successful(chrs)
        case _                                      => channelDAO.list(state = 1)
      }
      case None => Future.successful(Seq[ChannelRow]())
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ChannelService.listByUid($uid): ${e.getMessage}")
        Seq[ChannelRow]()
    }
  }

  def count(): Future[Option[Int]] = {
    channelDAO.count().map { c => Some(c) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ChannelService.count(): ${e.getMessage}")
        None
    }
  }

  def insert(channelRow: ChannelRow): Future[Option[Long]] = {
    channelDAO.insert(channelRow).map { id => Some(id) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ChannelService.insert(${channelRow.toString}): ${e.getMessage}")
        None
    }
  }

  def update(id: Long, channelRow: ChannelRow): Future[Option[ChannelRow]] = {
    channelDAO.update(id, channelRow).recover {
      case NonFatal(e) =>
        Logger.error(s"Within ChannelService.update($id, ${channelRow.toString}): ${e.getMessage}")
        None
    }
  }

  def delete(id: Long): Future[Option[Long]] = {
    channelDAO.delete(id).recover {
      case NonFatal(e) =>
        Logger.error(s"Within ChannelService.delete($id): ${e.getMessage}")
        None
    }
  }
}