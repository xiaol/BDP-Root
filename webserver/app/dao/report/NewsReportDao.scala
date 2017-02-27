package dao.report

import java.sql.Timestamp
import javax.inject.{ Inject, Singleton }

import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class NewsReportDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  def topClickNews(ctype: Int, ptype: Int, page: Long, count: Long): Future[Seq[(Long, String, Option[Int], Option[Int], Option[Int], Option[Int], Timestamp)]] = {
    val action = sql"select nc.nid, nv.title, nc.clickcount, nc.showcount, nc.ctype, nc.ptype, nc.data_time_count from newsclickorder nc inner join newslist_v2 nv on nc.nid=nv.nid where nc.ctype=$ctype and nc.ptype=$ptype and nc.ctime>now()-interval'30 day' order by data_time_count desc,clickcount desc offset $page limit $count ".as[(Long, String, Option[Int], Option[Int], Option[Int], Option[Int], Timestamp)]
    db.run(action)
  }

}
