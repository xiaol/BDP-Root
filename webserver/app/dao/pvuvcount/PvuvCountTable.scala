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
    def pv = column[Option[Long]]("pv")
    def data_time_count = column[LocalDateTime]("data_time_count")
    def androidpv = column[Option[Long]]("androidpv")
    def iospv = column[Option[Long]]("iospv")
    def androiduv = column[Option[Long]]("androiduv")
    def iosuv = column[Option[Long]]("iosuv")
    def adpv = column[Option[Long]]("adpv")
    def ctype = column[Option[Int]]("ctype")

    def * = (id, pv, data_time_count, androidpv, iospv, androiduv, iosuv, adpv, ctype) <> ((PvUvofDay.apply _).tupled, PvUvofDay.unapply)
  }
}

@Singleton
class PvuvCountDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends PvuvCountTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val pvuvCountList = TableQuery[PvuvCountTable]

  def pvuvCount(offset: Long, limit: Long): Future[Seq[PvUvofDay]] = {
    db.run(pvuvCountList.sortBy(r => (r.data_time_count.desc, r.ctype.asc)).drop(offset).take(limit).result)
  }

}