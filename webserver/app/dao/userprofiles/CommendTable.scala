package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.userprofiles.CommendRow
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait CommendTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class CommendTable(tag: Tag) extends Table[CommendRow](tag, "commendlist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def uid = column[Long]("uid")
    def cid = column[Long]("cid")

    def * = (id.?, ctime, uid, cid) <> ((CommendRow.apply _).tupled, CommendRow.unapply)
  }
}

@Singleton
class CommendDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CommendTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val commendList = TableQuery[CommendTable]

  def listByUid(uid: Long, offset: Long, limit: Long): Future[Seq[CommendRow]] = {
    db.run(commendList.filter(_.uid === uid).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def insert(commendRow: CommendRow): Future[Long] = {
    db.run(commendList returning commendList.map(_.id) += commendRow)
  }

  def delete(id: Long): Future[Option[Long]] = {
    db.run(commendList.filter(_.id === id).delete.map {
      case 0 => None
      case _ => Some(id)
    })
  }

  def delete(cid: Long, uid: Long): Future[Option[Long]] = {
    db.run(commendList.filter(_.uid === uid).filter(_.cid === cid).delete.map {
      case 0 => None
      case _ => Some(cid)
    })
  }
}