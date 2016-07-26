package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsRecommendRead
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangshl on 16/7/22.
 */
trait NewsRecommendReadTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsRecommendReadTable(tag: Tag) extends Table[NewsRecommendRead](tag, "newsrecommendread") {

    import commons.models.news._

    def uid = column[Long]("uid")
    def nid = column[Long]("nid")
    def readtime = column[LocalDateTime]("readtime")

    def * = (uid, nid, readtime) <> ((NewsRecommendRead.apply _).tupled, NewsRecommendRead.unapply)
  }
}

object NewsRecommendReadDAO {

}

@Singleton
class NewsRecommendReadDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsRecommendReadTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val newsRecommendReadList = TableQuery[NewsRecommendReadTable]

  def insert(newsRecommendRead: Seq[NewsRecommendRead]): Future[Seq[Long]] = {
    db.run(newsRecommendReadList returning newsRecommendReadList.map(_.nid) ++= newsRecommendRead)
  }

}