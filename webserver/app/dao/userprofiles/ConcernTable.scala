package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsRow
import commons.models.userprofiles.ConcernRow
import dao.news.NewsTable
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait ConcernTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class ConcernTable(tag: Tag) extends Table[ConcernRow](tag, "concernlist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def uid = column[Long]("uid")
    def nid = column[Long]("nid")

    def * = (id.?, ctime, uid, nid) <> ((ConcernRow.apply _).tupled, ConcernRow.unapply)
  }
}

@Singleton
class ConcernDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ConcernTable with NewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val concernList = TableQuery[ConcernTable]
  val newsList = TableQuery[NewsTable]

  def listNewsByConcernsAction(uid: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (cs, news) <- concernList.filter(_.uid === uid).sortBy(_.ctime.desc).drop(offset).take(limit)
        .joinLeft(newsList).on(_.nid === _.nid)
    } yield news
  }
  def listNewsByConcernsCompiled = Compiled(listNewsByConcernsAction _)
  def listNewsByConcerns(uid: Long, offset: Long, limit: Long): Future[Seq[NewsRow]] = {
    db.run(listNewsByConcernsCompiled(uid, offset, limit).result).map {
      case ns: Seq[Option[NewsRow]] => ns.filter(_.isDefined).map(_.get)
    }
  }

  def listByUid(uid: Long, offset: Long, limit: Long): Future[Seq[ConcernRow]] = {
    db.run(concernList.filter(_.uid === uid).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def insert(concernRow: ConcernRow): Future[Long] = {
    db.run(concernList returning concernList.map(_.id) += concernRow)
  }

  def delete(id: Long): Future[Option[Long]] = {
    db.run(concernList.filter(_.id === id).delete.map {
      case 0 => None
      case _ => Some(id)
    })
  }

  def delete(nid: Long, uid: Long): Future[Option[Long]] = {
    db.run(concernList.filter(_.uid === uid).filter(_.nid === nid).delete.map {
      case 0 => None
      case _ => Some(nid)
    })
  }
}