package dao.spiders

import javax.inject.{ Inject, Singleton }

import commons.models.spiders._
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver
import play.api.libs.json.JsValue
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait SourceTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class SourceTable(tag: Tag) extends Table[SourceRow](tag, "sourcelist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def surl = column[Option[String]]("surl")
    def sname = column[String]("sname")
    def descr = column[Option[String]]("descr")
    def queue = column[String]("queue")
    def rate = column[Int]("rate")
    def status = column[Int]("status")
    def cname = column[String]("cname")
    def cid = column[Long]("cid")
    def pconf = column[Option[JsValue]]("pconf")
    def state = column[Int]("state")

    def * = (id.?, ctime, surl, sname, descr, queue, rate, status, cname, cid, pconf, state) <> ((SourceRow.apply _).tupled, SourceRow.unapply)
  }
}

@Singleton
class SourceDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends SourceTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val sourceList = TableQuery[SourceTable]

  def list(offset: Long, limit: Long): Future[Seq[SourceRow]] = {
    db.run(sourceList.sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def listAll(): Future[Seq[SourceRow]] = {
    db.run(sourceList.sortBy(_.ctime.asc).result)
  }

  def listByStateAndStatus(state: Int, status: Int, offset: Long, limit: Long): Future[Seq[SourceRow]] = {
    db.run(sourceList.filter(_.state === state).filter(_.status === status).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def listByState(state: Int, offset: Long, limit: Long): Future[Seq[SourceRow]] = {
    db.run(sourceList.filter(_.state === state).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def listByQueue(queue: String, offset: Long, limit: Long): Future[Seq[SourceRow]] = {
    db.run(sourceList.sortBy(_.queue.asc).drop(offset).take(limit).result)
  }

  def findById(id: Long): Future[Option[SourceRow]] = {
    db.run(sourceList.filter(_.id === id).result.headOption)
  }

  def update(id: Long, sourceRow: SourceRow): Future[Option[SourceRow]] = {
    db.run(sourceList.filter(_.id === id).update(sourceRow.copy(id = Some(id))).map {
      case 0 => None
      case _ => Some(sourceRow)
    })
  }

  def insert(sourceRow: SourceRow): Future[Long] = {
    db.run(sourceList returning sourceList.map(_.id) += sourceRow)
  }

  def delete(id: Long): Future[Option[Long]] = {
    db.run(sourceList.filter(_.id === id).delete.map {
      case 0 => None
      case _ => Some(id)
    })
  }

  def count(): Future[Int] = {
    db.run(sourceList.length.result)
  }
}