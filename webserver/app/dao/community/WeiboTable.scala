package dao.community

import javax.inject.{ Inject, Singleton }
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver
import scala.concurrent.{ ExecutionContext, Future }

import commons.models.community.Weibo
import commons.models.community.WeiboRow

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait WeiboTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class WeiboTable(tag: Tag) extends Table[WeiboRow](tag, "weibolist") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def refer = column[String]("refer")

    def url = column[String]("url")
    def content = column[String]("content")
    def ptime = column[LocalDateTime]("ptime")
    def uame = column[String]("uame")
    def commend = column[Int]("commend")
    def repost = column[Int]("repost")
    def comment = column[Int]("comment")
    def avatar = column[Option[String]]("avatar")
    def img = column[Option[String]]("img")
    def imgs = column[Option[List[String]]]("imgs")

    def weibo = (url, content, ptime, uame, commend, repost, comment, avatar, img, imgs) <> ((Weibo.apply _).tupled, Weibo.unapply)
    def * = (id.?, ctime, refer, weibo) <> ((WeiboRow.apply _).tupled, WeiboRow.unapply)
  }
}

@Singleton
class WeiboDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends WeiboTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val weiboList = TableQuery[WeiboTable]

  def listByRefer(refer: String, offset: Long, limit: Long): Future[Seq[WeiboRow]] = {
    db.run(weiboList.filter(_.refer === refer).sortBy(_.ptime.desc).drop(offset).take(limit).result)
  }

  def deleteByRefer(refer: String): Future[Option[String]] = {
    db.run(weiboList.filter(_.refer === refer).delete.map {
      case 0 => None
      case _ => Some(refer)
    })
  }

  def insert(weiboRow: WeiboRow): Future[Long] = {
    db.run(weiboList returning weiboList.map(_.id) += weiboRow)
  }

  def insertAll(weiboRows: Seq[WeiboRow]): Future[Seq[Long]] = {
    db.run(weiboList returning weiboList.map(_.id) ++= weiboRows)
  }
}