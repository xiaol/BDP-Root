package dao.community

import javax.inject.{ Inject, Singleton }
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver
import scala.concurrent.{ ExecutionContext, Future }
import commons.models.community.ASearch
import commons.models.community.ASearchRow

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait ASearchTable { self: HasDatabaseConfig[MyPostgresDriver] =>

  import driver.api._

  class ASearchTable(tag: Tag) extends Table[ASearchRow](tag, "asearchlist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def refer = column[String]("refer")

    def url = column[String]("url")
    def title = column[String]("title")
    def from = column[String]("from")
    def rank = column[Int]("rank")
    def pname = column[String]("pname")
    def ptime = column[LocalDateTime]("ptime")
    def img = column[Option[String]]("img")
    def abs = column[Option[String]]("abs")
    def nid = column[Option[Long]]("nid")
    def duration = column[Option[Int]]("duration")
    def rtype = column[Option[Int]]("rtype")

    def searchItem = (url, title, from, rank, pname, ptime, img, abs, nid, duration, rtype, Some(26), Some(0)) <> ((ASearch.apply _).tupled, ASearch.unapply)
    def * = (id.?, ctime, refer, searchItem) <> ((ASearchRow.apply _).tupled, ASearchRow.unapply)
  }
}

@Singleton
class ASearchDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ASearchTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val asearchList = TableQuery[ASearchTable]

  def listByRefer(refer: String, offset: Long, limit: Long): Future[Seq[ASearchRow]] = {
    db.run(asearchList.filter(_.refer === refer).sortBy(_.ptime.desc).drop(offset).take(limit).result)
  }

  def deleteByRefer(refer: String): Future[Option[String]] = {
    db.run(asearchList.filter(_.refer === refer).delete.map {
      case 0 => None
      case _ => Some(refer)
    })
  }

  def insert(searchItemRow: ASearchRow): Future[Long] = {
    db.run(asearchList returning asearchList.map(_.id) += searchItemRow)
  }

  def insertAll(searchItemRows: Seq[ASearchRow]): Future[Seq[Long]] = {
    db.run(asearchList returning asearchList.map(_.id) ++= searchItemRows)
  }
}