package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.userprofiles.CommentRow
import dao.news.NewsTable
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-10.
 *
 */

trait CommentTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class CommentTable(tag: Tag) extends Table[CommentRow](tag, "commentlist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def commend = column[Int]("commend")
    def ctime = column[LocalDateTime]("ctime")
    def uid = column[Option[Long]]("uid")
    def uname = column[String]("uname")
    def avatar = column[Option[String]]("avatar")
    def docid = column[String]("docid")
    def cid = column[Option[String]]("cid")
    def pid = column[Option[String]]("pid")
    def global_id = column[Option[String]]("global_id")

    def * = (id.?, content, commend, ctime, uid, uname, avatar, docid, cid, pid, global_id) <> ((CommentRow.apply _).tupled, CommentRow.unapply)
  }
}

@Singleton
class CommentDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CommentTable with CommendTable with NewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val commentList = TableQuery[CommentTable]
  val commendList = TableQuery[CommendTable]
  val newsList = TableQuery[NewsTable]

  def findById(id: Long): Future[Option[CommentRow]] = {
    db.run(commentList.filter(_.id === id).result.headOption)
  }

  def listByUid(uid: Long, offset: Long, limit: Long): Future[Seq[CommentRow]] = {
    db.run(commentList.filter(_.uid === uid).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def listByUidWithNewsInfo(uid: Long, offset: Long, limit: Long): Future[Seq[(CommentRow, Long, String)]] = {
    val joinQuery = for {
      (comment, news) <- commentList.filter(_.uid === uid).sortBy(_.ctime.desc).drop(offset).take(limit)
        .joinLeft(newsList).on(_.docid === _.docid)
    } yield (comment, news.map(_.nid), news.map(_.title))

    for (pairs <- db.run(joinQuery.result)) yield {
      for (pair <- pairs) yield {
        pair match {
          case (comment, nids, titles) => (comment, nids.get, titles.get)
        }
      }
    }
  }

  def listByDocid(docid: String, offset: Long, limit: Long): Future[Seq[CommentRow]] = {
    db.run(commentList.filter(_.docid === docid).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  private def commentsJoinCommends(docid: ConstColumn[String], uid: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (comments, commends) <- commentList.filter(_.docid === docid).sortBy(_.ctime.desc).drop(offset).take(limit)
        .joinLeft(commendList.filter(_.uid === uid)).on(_.id === _.cid)
    } yield (comments, commends)
  }
  private val commentsJoinCommendsCompiled = Compiled(commentsJoinCommends _)
  def listByDocidAndUid(docid: String, uid: Long, offset: Long, limit: Long): Future[Seq[(CommentRow, Int)]] = {
    for (pairs <- db.run(commentsJoinCommendsCompiled(docid, uid, offset, limit).result)) yield {
      for (pair <- pairs) yield {
        pair match {
          case (comment, commend) if commend.isDefined => (comment, 1)
          case (comment, commend)                      => (comment, 0)
        }
      }
    }
  }

  def listByDocidHot(docid: String, offset: Long, limit: Long): Future[Seq[CommentRow]] = {
    db.run(commentList.filter(_.docid === docid).filter(_.commend > 0).sortBy(_.commend.desc).drop(offset).take(limit).result)
  }

  private def commentsHotJoinCommends(docid: ConstColumn[String], uid: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (comments, commends) <- commentList.filter(_.docid === docid).filter(_.commend > 0).sortBy(_.commend.desc).drop(offset).take(limit)
        .joinLeft(commendList.filter(_.uid === uid)).on(_.id === _.cid)
    } yield (comments, commends)
  }
  private val commentsHotJoinCommendsCompiled = Compiled(commentsHotJoinCommends _)
  def listByDocidAndUidHot(docid: String, uid: Long, offset: Long, limit: Long): Future[Seq[(CommentRow, Int)]] = {
    for (pairs <- db.run(commentsHotJoinCommendsCompiled(docid, uid, offset, limit).result)) yield {
      for (pair <- pairs) yield {
        pair match {
          case (comment, commend) if commend.isDefined => (comment, 1)
          case (comment, commend)                      => (comment, 0)
        }
      }
    }
  }

  def count(): Future[Int] = {
    db.run(commentList.length.result)
  }

  def insert(commentRow: CommentRow): Future[Long] = {
    db.run(commentList returning commentList.map(_.id) += commentRow)
  }

  def update(id: Long, commentRow: CommentRow): Future[Option[CommentRow]] = {
    db.run(commentList.filter(_.id === id).update(commentRow).map {
      case 0 => None
      case _ => Some(commentRow)
    })
  }

  def updateCommend(id: Long, uid: Long, commend: Int): Future[Option[Int]] = {
    val queryCommend = commentList.filter(_.id === id).map(_.commend) //.filter(_.uid =!= Some(uid))
    val commendOpt: Future[Option[Int]] = db.run(queryCommend.result.headOption)
    commendOpt.flatMap {
      case Some(c) => db.run(queryCommend.update(c + commend).map {
        case 0 => None
        case _ => Some(c + commend)
      })
      case _ => Future.successful(None)
    }
  }

  def delete(id: Long): Future[Option[Long]] = {
    db.run(commentList.filter(_.id === id).delete.map {
      case 0 => None
      case _ => Some(id)
    })
  }
}