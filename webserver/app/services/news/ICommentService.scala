package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.userprofiles.{ CommentResponse, CommentRow }
import commons.utils.JodaOderingImplicits
import dao.userprofiles.CommentDAO
import play.api.Logger

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[CommentService])
trait ICommentService {
  def findById(id: Long): Future[Option[CommentRow]]
  def listByUid(uid: Long, page: Long, offset: Long): Future[Seq[CommentRow]]
  def listByDocid(docid: String, page: Long, offset: Long): Future[Seq[CommentResponse]]
  def listByDocidAndUid(docid: String, uid: Long, page: Long, count: Long): Future[Seq[CommentResponse]]
  def listByDocidHot(docid: String, page: Long, offset: Long): Future[Seq[CommentResponse]]
  def listByDocidAndUidHot(docid: String, uid: Long, page: Long, count: Long): Future[Seq[CommentResponse]]
  def count(): Future[Option[Int]]
  def insert(commentRow: CommentRow): Future[Option[Long]]
  def delete(id: Long): Future[Option[Long]]
}

class CommentService @Inject() (val commentDAO: CommentDAO) extends ICommentService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def findById(id: Long): Future[Option[CommentRow]] = {
    commentDAO.findById(id).recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.findById($id): ${e.getMessage}")
        None
    }
  }

  def listByUid(uid: Long, page: Long, count: Long): Future[Seq[CommentRow]] = {
    commentDAO.listByUid(uid, (page - 1) * count, count).recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.listByUid($uid): ${e.getMessage}")
        Seq[CommentRow]()
    }
  }

  def listByDocid(docid: String, page: Long, count: Long): Future[Seq[CommentResponse]] = {
    commentDAO.listByDocid(docid, (page - 1) * count, count).map { case rows => rows.sortBy(_.ctime).map(CommentResponse.from(_, 0)) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.listByDocid($docid): ${e.getMessage}")
        Seq[CommentResponse]()
    }
  }

  def listByDocidAndUid(docid: String, uid: Long, page: Long, count: Long): Future[Seq[CommentResponse]] = {
    commentDAO.listByDocidAndUid(docid, uid, (page - 1) * count, count).map {
      case pairs: Seq[(CommentRow, Int)] => pairs.sortBy(_._1.ctime).map { case (row, flag) => CommentResponse.from(row, flag) }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.listByDocidAndUid($docid, $uid): ${e.getMessage}")
        Seq[CommentResponse]()
    }
  }

  def listByDocidHot(docid: String, page: Long, count: Long = 5L): Future[Seq[CommentResponse]] = {
    commentDAO.listByDocidHot(docid, (page - 1) * count, count).map { case rows => rows.map(CommentResponse.from(_, 0)) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.listByDocidHot($docid): ${e.getMessage}")
        Seq[CommentResponse]()
    }
  }

  def listByDocidAndUidHot(docid: String, uid: Long, page: Long, count: Long): Future[Seq[CommentResponse]] = {
    commentDAO.listByDocidAndUidHot(docid, uid, (page - 1) * count, count).map {
      case pairs: Seq[(CommentRow, Int)] => pairs.map { case (row, flag) => CommentResponse.from(row, flag) }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.listByDocidAndUidHot($docid, $uid): ${e.getMessage}")
        Seq[CommentResponse]()
    }
  }

  def count(): Future[Option[Int]] = {
    commentDAO.count().map { c => Some(c) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.count(): ${e.getMessage}")
        None
    }
  }

  def insert(commentRow: CommentRow): Future[Option[Long]] = {
    commentDAO.insert(commentRow).map { id => Some(id) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.insert(${commentRow.toString}): ${e.getMessage}")
        None
    }
  }

  def delete(id: Long): Future[Option[Long]] = {
    commentDAO.delete(id).recover {
      case NonFatal(e) =>
        Logger.error(s"Within CommentService.delete($id): ${e.getMessage}")
        None
    }
  }
}