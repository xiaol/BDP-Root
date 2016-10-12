package dao.news

import java.sql.Date
import javax.inject.{ Inject, Singleton }

import commons.models.news.Newsrecommendclick
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-09.
 *
 */

trait NewsrecommendclickTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsrecommendclickTable(tag: Tag) extends Table[Newsrecommendclick](tag, "newsrecommendclick") {
    def uid = column[Long]("uid")
    def nid = column[Long]("nid")
    def ctime = column[Date]("ctime")

    def * = (uid, nid, ctime) <> ((Newsrecommendclick.apply _).tupled, Newsrecommendclick.unapply)
  }
}

@Singleton
class NewsrecommendclickDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends NewsrecommendclickTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  lazy val people = TableQuery[NewsrecommendclickTable]

  def selectNewsrecommendclicks: Future[Seq[Newsrecommendclick]] = {
    val action = sql"select * from newsrecommendclick limit 10 ".as[Newsrecommendclick]
    db.run(action)
  }

}
