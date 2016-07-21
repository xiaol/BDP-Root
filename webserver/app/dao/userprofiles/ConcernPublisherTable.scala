package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.news.{ NewsPublisherRow, NewsRow }
import commons.models.userprofiles.ConcernPublisherRow
import commons.utils._
import dao.news.{ NewsPublisherTable, NewsTable }
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-07-14.
 *
 */

trait ConcernPublisherTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class ConcernPublisherTable(tag: Tag) extends Table[ConcernPublisherRow](tag, "concernpublisherlist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def uid = column[Long]("uid")
    def pname = column[String]("pname")

    def * = (id.?, ctime, uid, pname) <> ((ConcernPublisherRow.apply _).tupled, ConcernPublisherRow.unapply)
  }
}

@Singleton
class ConcernPublisherDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ConcernPublisherTable with NewsPublisherTable with NewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val concernPubList = TableQuery[ConcernPublisherTable]
  val publisherList = TableQuery[NewsPublisherTable]
  val newsList = TableQuery[NewsTable]

  private def listNewsPublisherByConcernsAction(uid: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (cs, pubs) <- concernPubList.filter(_.uid === uid).sortBy(_.ctime.desc).drop(offset).take(limit)
        .joinLeft(publisherList).on(_.pname === _.name)
    } yield pubs
  }
  private val listNewsPublisherByConcernsCompiled = Compiled(listNewsPublisherByConcernsAction _)

  def listNewsPublisherByConcerns(uid: Long, offset: Long, limit: Long): Future[Seq[NewsPublisherRow]] = {
    db.run(listNewsPublisherByConcernsCompiled(uid, offset, limit).result).map {
      case ns: Seq[Option[NewsPublisherRow]] => ns.filter(_.isDefined).map(_.get)
    }
  }

  private def updateConcernQuery(pname: String, concernValue: Int): DBIO[Int] = {
    publisherList.filter(_.name === pname).map(_.concern).result.headOption.flatMap {
      case Some(cc) => publisherList.filter(_.name === pname).map(_.concern).update(cc + concernValue).map {
        case 0 => throw PGDBException(NotFound("publisherList", ("name", pname)))
        case _ => cc + concernValue
      }
      case None => DBIO.failed(PGDBException(NotFound("publisherList", ("name", pname))))
    }
  }

  def insertAndUpdateNewsPublisherConcerns(uid: Long, pname: String): Future[Int] = {
    val concernValue: Int = 1
    val concernPublisherRow: ConcernPublisherRow = ConcernPublisherRow(None, LocalDateTime.now().withMillisOfSecond(0), uid, pname)

    val insertConcernAction: DBIO[Long] = concernPubList.filter(_.uid === uid).filter(_.pname === pname).map(_.id).result.headOption.flatMap {
      case Some(id) => throw PGDBException(AlreadyExist("concernPubList-iiiii", concernPublisherRow.toString))
      case None     => concernPubList returning concernPubList.map(_.id) += concernPublisherRow
    }

    db.run((for {
      insertResult <- insertConcernAction
      updateResult <- updateConcernQuery(pname, concernValue)
    } yield updateResult).transactionally)
  }

  def deleteAndUpdateNewsPublisherConcerns(uid: Long, pname: String): Future[Int] = {
    val concernValue: Int = -1

    val deleteConcernAction = concernPubList.filter(_.uid === uid).filter(_.pname === pname).delete.map {
      case 0 => throw PGDBException(NotFound("concernPubList-iiiiii", ("uid", uid.toString), ("pname", pname)))
      case c => c
    }

    db.run((for {
      deleteResult <- deleteConcernAction
      updateResult <- updateConcernQuery(pname, concernValue)
    } yield updateResult).transactionally)
  }

  private def loadNewsByConcernedPublishersAction(uid: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long], timeCursor: ConstColumn[LocalDateTime], timeWindow: ConstColumn[LocalDateTime]) = {
    for {
      (cs, news) <- concernPubList.filter(_.uid === uid)
        .joinLeft(newsList.filter(_.ctime > timeWindow).filter(_.ctime < timeCursor) //.sortBy(_.ctime.desc)
        ).on(_.pname === _.pname).sortBy(_._2.map(_.ctime).desc).drop(offset).take(limit)
    } yield news
  }
  private val loadNewsByConcernedPublishersCompiled = Compiled(loadNewsByConcernedPublishersAction _)

  def loadNewsByConcernedPublishers(uid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    val timeWindow: LocalDateTime = timeCursor.plusDays(-30)
    db.run(loadNewsByConcernedPublishersCompiled(uid, offset, limit, timeCursor, timeWindow).result).map {
      case ns: Seq[Option[NewsRow]] => ns.filter(_.isDefined).map(_.get)
    }
  }

  private def refreshNewsByConcernedPublishersAction(uid: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long], timeCursor: ConstColumn[LocalDateTime]) = {
    for {
      (cs, news) <- concernPubList.filter(_.uid === uid)
        .joinLeft(newsList.filter(_.ctime > timeCursor)
        ).on(_.pname === _.pname).sortBy(_._2.map(_.ctime).desc).drop(offset).take(limit)
    } yield news
  }
  private val refreshNewsByConcernedPublishersCompiled = Compiled(refreshNewsByConcernedPublishersAction _)

  def refreshNewsByConcernedPublishers(uid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(refreshNewsByConcernedPublishersCompiled(uid, offset, limit, timeCursor).result).map {
      case ns: Seq[Option[NewsRow]] => ns.filter(_.isDefined).map(_.get)
    }
  }
}