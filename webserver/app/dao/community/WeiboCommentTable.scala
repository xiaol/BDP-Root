package dao.community

import javax.inject.{ Inject, Singleton }
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver
import scala.concurrent.{ ExecutionContext, Future }

import commons.models.community._

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait WeiboCommentTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class WeiboCommentTable(tag: Tag) extends Table[WeiboCommentRow](tag, "weibocommentlist") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def refer = column[String]("refer")

    def content = column[String]("content")
    def ptime = column[LocalDateTime]("ptime")
    def commend = column[Int]("commend")
    def uname = column[String]("uname")
    def uid = column[String]("uid")
    def wid = column[String]("wid")
    def avatar = column[Option[String]]("avatar")

    def weiboComment = (content, ptime, commend, uname, uid, wid, avatar) <> ((WeiboComment.apply _).tupled, WeiboComment.unapply)
    def * = (id.?, ctime, refer, weiboComment) <> ((WeiboCommentRow.apply _).tupled, WeiboCommentRow.unapply)
  }
}

@Singleton
class WeiboCommentDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends WeiboCommentTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val weiboCommentList = TableQuery[WeiboCommentTable]

  def listByRefer(refer: String, offset: Long, limit: Long): Future[Seq[WeiboCommentRow]] = {
    db.run(weiboCommentList.filter(_.refer === refer).sortBy(_.ptime.desc).drop(offset).take(limit).result)
  }

  def deleteByRefer(refer: String): Future[Option[String]] = {
    db.run(weiboCommentList.filter(_.refer === refer).delete.map {
      case 0 => None
      case _ => Some(refer)
    })
  }

  def insert(weiboCommentRow: WeiboCommentRow): Future[Long] = {
    db.run(weiboCommentList returning weiboCommentList.map(_.id) += weiboCommentRow)
  }

  def insertAll(weiboCommentRows: Seq[WeiboCommentRow]): Future[Seq[Long]] = {
    db.run(weiboCommentList returning weiboCommentList.map(_.id) ++= weiboCommentRows)
  }
}