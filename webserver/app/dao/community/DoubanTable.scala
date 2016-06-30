package dao.community

import javax.inject.{ Inject, Singleton }
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver
import scala.concurrent.{ ExecutionContext, Future }

import commons.models.community.Douban
import commons.models.community.DoubanRow

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait DoubanTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class DoubanTable(tag: Tag) extends Table[DoubanRow](tag, "doubanlist") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def refer = column[String]("refer")

    def title = column[String]("title")
    def url = column[String]("url")

    def douban = (title, url) <> ((Douban.apply _).tupled, Douban.unapply)
    def * = (id.?, ctime, refer, douban) <> ((DoubanRow.apply _).tupled, DoubanRow.unapply)
  }
}

@Singleton
class DoubanDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends DoubanTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val doubanList = TableQuery[DoubanTable]

  def findByRefer(refer: String): Future[Option[DoubanRow]] = {
    db.run(doubanList.filter(_.refer === refer).result.headOption)
  }

  def deleteByRefer(refer: String): Future[Option[String]] = {
    db.run(doubanList.filter(_.refer === refer).delete.map {
      case 0 => None
      case _ => Some(refer)
    })
  }

  def insert(doubanRow: DoubanRow): Future[Long] = {
    db.run(doubanList returning doubanList.map(_.id) += doubanRow)
  }
}