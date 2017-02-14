package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.{ NewsPublisherRow, NewsRow }
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
class NewsPublisherDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsPublisherTable with NewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val publisherList = TableQuery[NewsPublisherTable]
  val newsList = TableQuery[NewsTable]

  def findByName(name: String): Future[Option[NewsPublisherRow]] =
    db.run(publisherList.filter(_.name === name).result.map(_.headOption))

  def insert(newsPublisherRow: NewsPublisherRow): Future[Long] =
    db.run(publisherList returning publisherList.map(_.id) += newsPublisherRow)

  def insertOrDiscard(newsPublisherRow: NewsPublisherRow): Future[Long] = {
    val queryAction = publisherList.filter(_.name === newsPublisherRow.name).map(_.id).result.map(_.headOption).flatMap {
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

  def listNewsByPublisher(pname: String, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    val timeWindows = timeWindow(timeCursor, -3)
    db.run(newsList.filter(_.pname === pname).filter(_.ctime > timeWindows).filter(_.ctime < timeCursor).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def listNewsByPublisherWithPubInfo(pname: String, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[(NewsPublisherRow, Seq[NewsRow])] = {
    val timeWindows = timeWindow(timeCursor, -3)
    val queryAction = for {
      (pub, news) <- publisherList.filter(_.name === pname)
        .joinLeft(newsList.filter(_.pname === pname).filter(_.ctime > timeWindows).filter(_.ctime < timeCursor)).on(_.name === _.pname).sortBy(_._2.map(_.ctime).desc).drop(offset).take(limit)
    } yield (pub, news)

    db.run(queryAction.result).map {
      case pairs: Seq[(NewsPublisherRow, Option[NewsRow])] =>
        val result = pairs.groupBy(_._1).map { case (p, nOptSeq) => (p, nOptSeq.map(_._2).collect { case nOpt if nOpt.isDefined => nOpt.get }) }.headOption
        if (result.isDefined) result.get // && result.get._2.nonEmpty
        else throw PGDBException(NotFound("publisherList or newslist", ("pname", pname)))
    }
  }

  final private val timeWindow = (timeCursor: LocalDateTime, days: Int) => timeCursor.plusDays(days)
}

