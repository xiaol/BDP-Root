package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.channels.ChannelOrderRow
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangshl on 2017/5/2.
 */

trait ChannelOrderTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class ChannelOrderTable(tag: Tag) extends Table[ChannelOrderRow](tag, "channellist_channel") {
    def id = column[Long]("id")
    def cname = column[String]("cname")
    def state = column[Int]("state")
    def des = column[Option[String]]("des")
    def channel = column[Int]("channel")
    def order_num = column[Int]("order_num")

    def * = (id, cname, state, des, channel, order_num) <> ((ChannelOrderRow.apply _).tupled, ChannelOrderRow.unapply)
  }
}

@Singleton
class ChannelOrderDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends ChannelOrderTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val channelList = TableQuery[ChannelOrderTable]

  def listByChannel(channel: Int): Future[Seq[ChannelOrderRow]] = {
    db.run(channelList.filter(_.state === 1).filter(_.channel === channel).sortBy(_.order_num.asc).result)
  }

}
