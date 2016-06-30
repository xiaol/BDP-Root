package dao.spiders

import javax.inject.{ Inject, Singleton }
import commons.models.spiders._
import play.api.db.slick._
import utils.MyPostgresDriver
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait QueueTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class QueueTable(tag: Tag) extends Table[QueueRow](tag, "queuelist_v2") {
    def queue = column[String]("queue")
    def spider = column[String]("spider")
    def descr = column[Option[String]]("descr")

    def * = (queue, spider, descr) <> ((QueueRow.apply _).tupled, QueueRow.unapply)
  }
}

@Singleton
class QueueDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends QueueTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val queueList = TableQuery[QueueTable]

  def list(offset: Long, limit: Long): Future[Seq[QueueRow]] = {
    db.run(queueList.sortBy(_.queue.asc).drop(offset).take(limit).result)
  }

  def findByQueue(queue: String): Future[Option[QueueRow]] = {
    db.run(queueList.filter(_.queue === queue).result.headOption)
  }

  def listBySpider(spider: String, offset: Long, limit: Long): Future[Seq[QueueRow]] = {
    db.run(queueList.filter(_.spider === spider).sortBy(_.queue.asc).drop(offset).take(limit).result)
  }

  def insert(queueRow: QueueRow): Future[String] = {
    db.run(queueList returning queueList.map(_.queue) += queueRow)
  }

  def update(queue: String, queueRow: QueueRow): Future[Option[QueueRow]] = {
    db.run(queueList.filter(_.queue === queue).update(queueRow).map {
      case 0 => None
      case _ => Some(queueRow)
    })
  }

  def delete(queue: String): Future[Option[String]] = {
    db.run(queueList.filter(_.queue === queue).delete.map {
      case 0 => None
      case _ => Some(queue)
    })
  }

  def count(): Future[Int] = {
    db.run(queueList.length.result)
  }
}