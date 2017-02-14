package dao.community

import javax.inject.{ Inject, Singleton }
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver
import scala.concurrent.{ ExecutionContext, Future }

import commons.models.community.Baike
import commons.models.community.BaikeRow

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait BaikeTable { self: HasDatabaseConfig[MyPostgresDriver] =>

  import driver.api._

  class BaikeTable(tag: Tag) extends Table[BaikeRow](tag, "baikelist") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def refer = column[String]("refer")

    def title = column[String]("title")
    def url = column[String]("url")
    def abs = column[String]("abs")

    def baike = (title, url, abs) <> ((Baike.apply _).tupled, Baike.unapply)
    def * = (id.?, ctime, refer, baike) <> ((BaikeRow.apply _).tupled, BaikeRow.unapply)
  }
}

@Singleton
class BaikeDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends BaikeTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val baikeList = TableQuery[BaikeTable]

  def findByRefer(refer: String): Future[Option[BaikeRow]] = {
    db.run(baikeList.filter(_.refer === refer).result.map(_.headOption))
  }

  def deleteByRefer(refer: String): Future[Option[String]] = {
    db.run(baikeList.filter(_.refer === refer).delete.map {
      case 0 => None
      case _ => Some(refer)
    })
  }

  def insert(baikeRow: BaikeRow): Future[Long] = {
    db.run(baikeList returning baikeList.map(_.id) += baikeRow)
  }
}