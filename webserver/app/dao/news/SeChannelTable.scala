package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.channels.{ ChannelRow, SeChannelRow }
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-07-26.
 *
 */

trait SeChannelTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class SeChannelTable(tag: Tag) extends Table[SeChannelRow](tag, "sechannellist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def cname = column[String]("cname")
    def chid = column[Long]("chid")
    def state = column[Int]("state")
    def des = column[Option[String]]("des")

    def cnameUniqueIdx = index("se_cname_unique_idx", cname, unique = true)

    def * = (id.?, cname, chid, state, des) <> ((SeChannelRow.apply _).tupled, SeChannelRow.unapply)
  }
}

@Singleton
class SeChannelDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends SeChannelTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val seChannelList = TableQuery[SeChannelTable]

  def listByChid(chid: Long): Future[Seq[SeChannelRow]] = {
    db.run(seChannelList.filter(_.chid === chid).sortBy(_.id.asc).result)
  }
}