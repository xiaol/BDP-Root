package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.{ NewsRecommendHot, NewsRecommendRead }
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangshl on 16/7/22.
 */
trait NewsRecommendHotTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsRecommendHotTable(tag: Tag) extends Table[NewsRecommendHot](tag, "newsrecommendhot") {

    import commons.models.news._

    def nid = column[Long]("nid")
    def ctime = column[LocalDateTime]("ctime")
    def status = column[Option[Int]]("status")

    def * = (nid, ctime, status) <> ((NewsRecommendHot.apply _).tupled, NewsRecommendHot.unapply)
  }
}

object NewsRecommendHotDAO {

}

@Singleton
class NewsRecommendHotDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsRecommendHotTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val newsRecommendHotList = TableQuery[NewsRecommendHotTable]

  def insert(newsRecommendHot: Seq[NewsRecommendHot]): Future[Seq[Long]] = {
    db.run(newsRecommendHotList returning newsRecommendHotList.map(_.nid) ++= newsRecommendHot)
  }

}