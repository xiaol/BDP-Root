package dao.userprofiles

import javax.inject.{ Inject, Singleton }

import commons.models.userprofiles.Searchnewslist
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-11.
 *
 */

trait SearchNewsTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class SearchNewsTable(tag: Tag) extends Table[Searchnewslist](tag, "searchnewslist") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def uid = column[Long]("uid")
    def keywords = column[String]("keywords")
    def ctime = column[Option[LocalDateTime]]("ctime")

    def * = (id.?, uid, keywords, ctime) <> ((Searchnewslist.apply _).tupled, Searchnewslist.unapply)
  }
}

@Singleton
class SearchNewsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends SearchNewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._

  val searchList = TableQuery[SearchNewsTable]

  def insert(searchnews: Searchnewslist): Future[Int] = {
    db.run(searchList returning searchList.map(_.id) += searchnews)
  }

}
