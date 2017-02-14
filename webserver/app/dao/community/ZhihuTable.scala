package dao.community

import javax.inject.{ Inject, Singleton }
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver
import scala.concurrent.{ ExecutionContext, Future }

import commons.models.community.Zhihu
import commons.models.community.ZhihuRow

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait ZhihuTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class ZhihuTable(tag: Tag) extends Table[ZhihuRow](tag, "zhihulist") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def refer = column[String]("refer")

    def url = column[String]("url")
    def title = column[String]("title")
    def author = column[String]("author")

    def zhihu = (url, title, author) <> ((Zhihu.apply _).tupled, Zhihu.unapply)
    def * = (id.?, ctime, refer, zhihu) <> ((ZhihuRow.apply _).tupled, ZhihuRow.unapply)
  }
}

@Singleton
class ZhihuDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ZhihuTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val zhihuList = TableQuery[ZhihuTable]

  def findByRefer(refer: String): Future[Option[ZhihuRow]] = {
    db.run(zhihuList.filter(_.refer === refer).result.map(_.headOption))
  }

  def deleteByRefer(refer: String): Future[Option[String]] = {
    db.run(zhihuList.filter(_.refer === refer).delete.map {
      case 0 => None
      case _ => Some(refer)
    })
  }

  def insert(zhihuRow: ZhihuRow): Future[Long] = {
    db.run(zhihuList returning zhihuList.map(_.id) += zhihuRow)
  }
}