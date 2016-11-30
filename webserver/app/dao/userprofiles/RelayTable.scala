package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.userprofiles.Relaylist
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait RelayTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class RelayTable(tag: Tag) extends Table[Relaylist](tag, "relaylist") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def nid = column[Long]("nid")
    def uid = column[Long]("uid")
    def whereabout = column[Int]("whereabout")
    def ctime = column[Option[LocalDateTime]]("ctime")

    def * = (id.?, nid, uid, whereabout, ctime) <> ((Relaylist.apply _).tupled, Relaylist.unapply)
  }
}

@Singleton
class RelayNewsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends RelayTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val relayList = TableQuery[RelayTable]

  def insert(relaynews: Relaylist): Future[Int] = {
    db.run(relayList returning relayList.map(_.id) += relaynews)
  }

}
