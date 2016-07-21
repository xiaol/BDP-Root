package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsPublisherRow
import commons.utils._
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-07-13.
 *
 */

trait NewsPublisherTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsPublisherTable(tag: Tag) extends Table[NewsPublisherRow](tag, "newspublisherlist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def name = column[String]("name")
    def icon = column[Option[String]]("icon")
    def descr = column[Option[String]]("descr")
    def concern = column[Int]("concern")

    def * = (id.?, ctime, name, icon, descr, concern) <> ((NewsPublisherRow.apply _).tupled, NewsPublisherRow.unapply)
  }
}

@Singleton
class NewsPublisherDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsPublisherTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val publisherList = TableQuery[NewsPublisherTable]

  def findByName(name: String): Future[Option[NewsPublisherRow]] =
    db.run(publisherList.filter(_.name === name).result.headOption)

  def insert(newsPublisherRow: NewsPublisherRow): Future[Long] =
    db.run(publisherList returning publisherList.map(_.id) += newsPublisherRow)

  def insertOrDiscard(newsPublisherRow: NewsPublisherRow): Future[Long] = {
    val queryAction = publisherList.filter(_.name === newsPublisherRow.name).map(_.id).result.headOption.flatMap {
      case Some(id) => throw PGDBException(AlreadyExist("publisherList", newsPublisherRow.name))
      case None     => publisherList returning publisherList.map(_.id) += newsPublisherRow
    }.transactionally
    db.run(queryAction)
  }

  def delete(npid: Long): Future[Option[Long]] = {
    db.run(publisherList.filter(_.id === npid).delete.map {
      case 0 => None
      case _ => Some(npid)
    })
  }
}

