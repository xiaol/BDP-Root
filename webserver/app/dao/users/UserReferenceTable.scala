package dao.users

import javax.inject.{ Inject, Singleton }

import commons.models.users._
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-04-19.
 *
 */

trait UserReferenceTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class UserReferenceTable(tag: Tag) extends Table[UserReferenceRow](tag, "usergloblelist") {
    def id = column[Long]("id")
    def uid = column[String]("uid")
    def uname = column[Option[String]]("uname")
    def sys_source = column[String]("sys_source")
    def global_id = column[String]("global_id")
    def ctime = column[LocalDateTime]("ctime")

    def * = (id, uid, uname, sys_source, global_id, ctime) <> ((UserReferenceRow.apply _).tupled, UserReferenceRow.unapply)
  }
}

@Singleton
class UserReferenceDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserReferenceTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val userReferenceList = TableQuery[UserReferenceTable]

  def findByUid(uid: String, sys_source: String): Future[Option[UserReferenceRow]] = {
    db.run(userReferenceList.filter(_.uid === uid).filter(_.sys_source === sys_source).result.headOption)
  }

  def findByGlobal_id(global_id: String): Future[Option[UserReferenceRow]] = {
    db.run(userReferenceList.filter(_.global_id === global_id).result.headOption)
  }

  def insert(userReferenceRow: UserReferenceRow): Future[String] = {
    db.run(userReferenceList returning userReferenceList.map(_.global_id) += userReferenceRow)
  }

  def update(global_id: String, userReferenceRow: UserReferenceRow): Future[Option[UserReferenceRow]] = {
    db.run(userReferenceList.filter(_.global_id === global_id).update(userReferenceRow).map {
      case 0 => None
      case _ => Some(userReferenceRow)
    })
  }

  def delete(global_id: String): Future[Option[String]] = {
    db.run(userReferenceList.filter(_.global_id === global_id).delete.map {
      case 0 => None
      case _ => Some(global_id)
    })
  }
}
