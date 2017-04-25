package dao.updateversion

import javax.inject.{ Inject, Singleton }

import commons.models.updateversion.UpdateVersion
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangshl on 2017-04-24.
 *
 */

trait UpdateVersionTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class UpdateVersionTable(tag: Tag) extends Table[UpdateVersion](tag, "update_version") {

    def ctype = column[Int]("ctype")
    def ptype = column[Int]("ptype")
    def version = column[String]("version")
    def version_code = column[Int]("version_code")
    def updateLog = column[Option[String]]("updatelog")
    def downloadLink = column[String]("downloadlink")
    def forceUpdate = column[Boolean]("forceupdate")

    def * = (ctype, ptype, version, version_code, updateLog, downloadLink, forceUpdate) <> ((UpdateVersion.apply _).tupled, UpdateVersion.unapply)
  }
}

@Singleton
class UpdateVersionDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UpdateVersionTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val updateVersionList = TableQuery[UpdateVersionTable]

  def findUpdateVersion(ctype: Int, ptype: Int): Future[Option[UpdateVersion]] = {
    db.run(updateVersionList.filter(_.ctype === ctype).filter(_.ptype === ptype).result.map(_.headOption))
  }

  def insert(updateVersion: UpdateVersion): Future[Int] = {
    db.run(updateVersionList returning updateVersionList.map(_.ctype) += updateVersion)
  }

  def update(updateVersion: UpdateVersion): Future[Option[UpdateVersion]] = {
    db.run(updateVersionList.filter(_.ctype === updateVersion.ctype).filter(_.ptype === updateVersion.ptype).update(updateVersion).map {
      case 0 => None
      case _ => Some(updateVersion)
    })
  }

}