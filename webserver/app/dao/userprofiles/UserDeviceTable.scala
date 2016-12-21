package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.userprofiles.UserDevice
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-12-20.
 *
 */

trait UserDeviceTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class UserDeviceTable(tag: Tag) extends Table[UserDevice](tag, "user_device") {
    def uid = column[String]("uid")
    def imei = column[Option[String]]("imei")
    def imeiori = column[Option[String]]("imeiori")
    def mac = column[Option[String]]("mac")
    def macori = column[Option[String]]("macori")
    def mac1 = column[Option[String]]("mac1")
    def idfa = column[Option[String]]("idfa")
    def idfaori = column[Option[String]]("idfaori")
    def aaid = column[Option[String]]("aaid")
    def anid = column[Option[String]]("anid")
    def anidori = column[Option[String]]("anidori")
    def udid = column[Option[String]]("udid")
    def brand = column[Option[String]]("brand")
    def platform = column[Option[String]]("platform")
    def os = column[Option[String]]("os")
    def os_version = column[Option[String]]("os_version")
    def device_size = column[Option[String]]("device_size")
    def network = column[Option[String]]("network")
    def operator = column[Option[String]]("operator")
    def longitude = column[Option[String]]("longitude")
    def latitude = column[Option[String]]("latitude")
    def screen_orientation = column[Option[String]]("screen_orientation")

    def * = (uid, imei, imeiori, mac, macori, mac1, idfa, idfaori,
      aaid, anid, anidori, udid, brand, platform, os, os_version,
      device_size, network, operator, longitude, latitude, screen_orientation) <> ((UserDevice.apply _).tupled, UserDevice.unapply)
  }
}

@Singleton
class UserDeviceDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserDeviceTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val userDeviceList = TableQuery[UserDeviceTable]

  def insert(userDevice: UserDevice): Future[String] = {
    db.run(userDeviceList returning userDeviceList.map(_.uid) += userDevice)
  }

  def findByuid(uid: String): Future[Option[UserDevice]] = {
    db.run(userDeviceList.filter(_.uid === uid).result.headOption)
  }

}
