package dao.pvuvcount

import javax.inject.{ Inject, Singleton }

import commons.models.pvuv.PvUvofDay
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

trait PvuvCountTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class PvuvCountTable(tag: Tag) extends Table[PvUvofDay](tag, "pvuvcount") {

    def id = column[Int]("id")
    def pv = column[Long]("pv")
    def data_time_count = column[LocalDateTime]("data_time_count")
    def androidpv = column[Long]("androidpv")
    def iospv = column[Long]("iospv")
    def androiduv = column[Long]("androiduv")
    def iosuv = column[Long]("iosuv")

    def * = (id, pv, data_time_count, androidpv, iospv, androiduv, iosuv) <> ((PvUvofDay.apply _).tupled, PvUvofDay.unapply)
  }
}

@Singleton
class PvuvCountDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends PvuvCountTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val pvuvCountList = TableQuery[PvuvCountTable]

  def pvuvCount(offset: Long, limit: Long): Future[Seq[PvUvofDay]] = {
    db.run(pvuvCountList.sortBy(_.data_time_count.desc).drop(offset).take(limit).result)
  }

}