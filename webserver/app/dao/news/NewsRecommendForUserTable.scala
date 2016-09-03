package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.{ NewsRecommendForUser, NewsRecommendRead }
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangshl on 16/7/22.
 */
trait NewsRecommendForUserTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsRecommendForUserTable(tag: Tag) extends Table[NewsRecommendForUser](tag, "newsrecommendforuser") {

    import commons.models.news._

    def uid = column[Long]("uid")
    def nid = column[Long]("nid")
    def predict = column[Double]("predict")
    def ctime = column[LocalDateTime]("ctime")

    def * = (uid, nid, predict, ctime) <> ((NewsRecommendForUser.apply _).tupled, NewsRecommendForUser.unapply)
  }
}

object NewsRecommendForUserDAO {

}

@Singleton
class NewsRecommendForUserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsRecommendForUserTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val newsRecommendForUserList = TableQuery[NewsRecommendForUserTable]

}