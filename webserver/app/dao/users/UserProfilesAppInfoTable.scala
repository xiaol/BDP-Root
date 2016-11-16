package dao.users

import javax.inject.{ Inject, Singleton }

import commons.models.userprofiles.AppInfo
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangsl on 2016-11-16.
 *
 */

trait AppInfoTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class AppInfoTable(tag: Tag) extends Table[AppInfo](tag, "user_phone_apps") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def uid = column[Option[Long]]("uid")
    def app_id = column[Option[String]]("app_id")
    def app_name = column[Option[String]]("app_name")
    def active = column[Option[Int]]("active")
    def ctime = column[Option[LocalDateTime]]("ctime")

    def * = (id.?, uid, app_id, app_name, active, ctime) <> ((AppInfo.apply _).tupled, AppInfo.unapply)
  }
}

@Singleton
class AppInfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends AppInfoTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val appInfoList = TableQuery[AppInfoTable]

  def insert(appInfo: AppInfo): Future[Long] = {
    db.run(appInfoList returning appInfoList.map(_.id) += appInfo)
  }

  def delete(uid: Long): Future[Option[Long]] = {
    db.run(appInfoList.filter(_.uid === uid).delete.map {
      case 0 => None
      case _ => Some(uid)
    })
  }
}
