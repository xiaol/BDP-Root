package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsRow
import commons.models.userprofiles.CollectRow
import dao.news.NewsTable
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait CollectTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class CollectTable(tag: Tag) extends Table[CollectRow](tag, "collectlist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def uid = column[Long]("uid")
    def nid = column[Long]("nid")

    def * = (id.?, ctime, uid, nid) <> ((CollectRow.apply _).tupled, CollectRow.unapply)
  }
}

@Singleton
class CollectDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CollectTable with NewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val collectList = TableQuery[CollectTable]
  val newsList = TableQuery[NewsTable]

  def listNewsByCollectsAction(uid: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (cs, news) <- collectList.filter(_.uid === uid).sortBy(_.ctime.desc).drop(offset).take(limit)
        .joinLeft(newsList).on(_.nid === _.nid)
    } yield news
  }
  def listNewsByCollectsCompiled = Compiled(listNewsByCollectsAction _)
  def listNewsByCollects(uid: Long, offset: Long, limit: Long): Future[Seq[NewsRow]] = {
    db.run(listNewsByCollectsCompiled(uid, offset, limit).result).map {
      case ns: Seq[Option[NewsRow]] => ns.filter(_.isDefined).map(_.get)
    }
  }

  def listByUid(uid: Long, offset: Long, limit: Long): Future[Seq[CollectRow]] = {
    db.run(collectList.filter(_.uid === uid).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def insert(collectRow: CollectRow): Future[Long] = {
    db.run(collectList returning collectList.map(_.id) += collectRow)
  }

  def delete(id: Long): Future[Option[Long]] = {
    db.run(collectList.filter(_.id === id).delete.map {
      case 0 => None
      case _ => Some(id)
    })
  }

  def delete(nid: Long, uid: Long): Future[Option[Long]] = {
    db.run(collectList.filter(_.uid === uid).filter(_.nid === nid).delete.map {
      case 0 => None
      case _ => Some(nid)
    })
  }
}
