package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.channels.ChannelRow
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-09.
 *
 */

trait ChannelTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class ChannelTable(tag: Tag) extends Table[ChannelRow](tag, "channellist_v2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def cname = column[String]("cname")
    def state = column[Int]("state")
    def des = column[Option[String]]("des")

    def cnameUniqueIdx = index("cname_unique_idx", cname, unique = true)

    def * = (id.?, cname, state, des) <> ((ChannelRow.apply _).tupled, ChannelRow.unapply)
  }
}

@Singleton
class ChannelDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ChannelTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val channelList = TableQuery[ChannelTable]

  def list(state: Int): Future[Seq[ChannelRow]] = {
    db.run(channelList.filter(_.state === state).sortBy(_.id.asc).result)
  }

  def listByNames(names: List[String]): Future[Seq[ChannelRow]] = {
    db.run(channelList.filter(_.cname inSet names).sortBy(_.id.asc).result)
  }

  def count(): Future[Int] = {
    db.run(channelList.length.result)
  }

  def insert(channelRow: ChannelRow): Future[Long] = {
    db.run(channelList returning channelList.map(_.id) += channelRow)
  }

  def update(id: Long, channelRow: ChannelRow): Future[Option[ChannelRow]] = {
    db.run(channelList.filter(_.id === id).update(channelRow).map {
      case 0 => None
      case _ => Some(channelRow)
    })
  }

  def delete(id: Long): Future[Option[Long]] = {
    db.run(channelList.filter(_.id === id).delete.map {
      case 0 => None
      case _ => Some(id)
    })
  }
}
