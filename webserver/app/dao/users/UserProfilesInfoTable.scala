package dao.users

import javax.inject.{ Inject, Singleton }

import commons.models.userprofiles.UserProfilesInfo
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangsl on 2016-11-16.
 *
 */

trait UserProfilesInfoTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class UserProfilesInfoTable(tag: Tag) extends Table[UserProfilesInfo](tag, "user_profile") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def uid = column[Long]("uid")
    def province = column[Option[String]]("province")
    def city = column[Option[String]]("city")
    def area = column[Option[String]]("area")
    def brand = column[Option[String]]("brand")
    def model = column[Option[String]]("model")
    def ctime = column[Option[LocalDateTime]]("ctime")

    def * = (id.?, uid, province, city, area, brand, model, ctime) <> ((UserProfilesInfo.apply _).tupled, UserProfilesInfo.unapply)
  }
}

@Singleton
class UserProfilesInfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserProfilesInfoTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val userProfilesList = TableQuery[UserProfilesInfoTable]

  def insert(userProfiles: UserProfilesInfo): Future[Long] = {
    db.run(userProfilesList returning userProfilesList.map(_.uid) += userProfiles)
  }

  def delete(uid: Long): Future[Option[Long]] = {
    db.run(userProfilesList.filter(_.uid === uid).delete.map {
      case 0 => None
      case _ => Some(uid)
    })
  }

  def findByUid(uid: Long): Future[Option[UserProfilesInfo]] = {
    db.run(userProfilesList.filter(_.uid === uid).result.headOption)
  }
}
