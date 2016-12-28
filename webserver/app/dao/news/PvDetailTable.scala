package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.PvDetail
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-09.
 *
 */

trait PvDetailTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class PvDetailTable(tag: Tag) extends Table[PvDetail](tag, "pvdetail") {
    def uid = column[Long]("uid")
    def method = column[String]("method")
    def ctime = column[LocalDateTime]("ctime")
    def ipaddress = column[Option[String]]("ipaddress")

    def * = (uid, method, ctime, ipaddress) <> ((PvDetail.apply _).tupled, PvDetail.unapply)
  }
}

@Singleton
class PvDetailDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends PvDetailTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val pvDetailList = TableQuery[PvDetailTable]

  def insert(pvDetail: PvDetail): Future[Long] = {
    db.run(pvDetailList returning pvDetailList.map(_.uid) += pvDetail)
  }
}