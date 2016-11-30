package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsRecommendLike
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.ExecutionContext

/**
 * Created by zhangshl on 16/11/30.
 */
trait NewsRecommendLikeTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsRecommendLikeTable(tag: Tag) extends Table[NewsRecommendLike](tag, "newsrecommendlike") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def uid = column[Long]("uid")
    def nid = column[Long]("nid")
    def predict = column[Double]("predict")
    def ctime = column[LocalDateTime]("ctime")

    def * = (id.?, uid, nid, predict, ctime) <> ((NewsRecommendLike.apply _).tupled, NewsRecommendLike.unapply)
  }
}

@Singleton
class NewsRecommendLikeDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsRecommendLikeTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val newsRecommendLikeList = TableQuery[NewsRecommendLikeTable]

}