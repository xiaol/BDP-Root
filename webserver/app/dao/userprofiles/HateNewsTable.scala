package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsRow
import commons.models.userprofiles.Hatenewslist
import dao.news.NewsTable
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait HateNewsTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class HateNewsTable(tag: Tag) extends Table[Hatenewslist](tag, "hatenewslist") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def nid = column[Long]("nid")
    def uid = column[Long]("uid")
    def reason = column[Option[Int]]("reason")
    def ctime = column[Option[LocalDateTime]]("ctime")

    def * = (id.?, nid, uid, reason, ctime) <> ((Hatenewslist.apply _).tupled, Hatenewslist.unapply)
  }
}

@Singleton
class HateNewsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HateNewsTable with NewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val hatenewsList = TableQuery[HateNewsTable]
  val newsList = TableQuery[NewsTable]

  def insert(hatenews: Hatenewslist): Future[Int] = {
    db.run(hatenewsList returning hatenewsList.map(_.id) += hatenews)
  }

  def getNewsByUid(uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.ctime > LocalDateTime.now().plusDays(-7)).filter(_.nid in hatenewsList.filter(_.uid === uid).filter(_.ctime > LocalDateTime.now().plusMonths(-1)).map(_.nid)).result)
  }

}
