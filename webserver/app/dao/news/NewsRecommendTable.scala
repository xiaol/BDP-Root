package dao.news

import javax.inject.{ Inject, Singleton }

import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json.JsValue
import commons.models.news._
import commons.utils.JodaOderingImplicits

/**
 * Created by zhangshl on 16/7/15.
 */
trait NewsRecommendTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsRecommendTable(tag: Tag) extends Table[NewsRecommend](tag, "newsrecommendlist") {

    import commons.models.news._

    def nid = column[Long]("nid", O.PrimaryKey)
    def rtime = column[Option[LocalDateTime]]("rtime")
    def level = column[Option[Double]]("level")
    def bigimg = column[Option[Int]]("bigimg")
    def status = column[Option[Int]]("status")

    def * = (nid, rtime, level, bigimg, status) <> ((NewsRecommend.apply _).tupled, NewsRecommend.unapply)
  }
}

object NewsRecommendDAO {

}

@Singleton
class NewsRecommendDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsRecommendTable with NewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._
  import NewsDAO._

  val newsRecommendList = TableQuery[NewsRecommendTable]
  val newsList = TableQuery[NewsTable]

  def insert(newsRecommend: NewsRecommend): Future[Long] = {
    db.run(newsRecommendList returning newsRecommendList.map(_.nid) += newsRecommend)
  }

  def delete(nid: Long): Future[Option[Long]] = {
    db.run(newsRecommendList.filter(_.nid === nid).delete.map {
      case 0 => None
      case _ => Some(nid)
    })
  }

  private def newsFilternewsRecommend(channel: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      news <- newsList.filter(_.channel === channel).filter(_.ctime > LocalDateTime.now().plusDays(-7)).filterNot(_.nid in newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24)).map(_.nid)).sortBy(_.ctime.desc).drop(offset).take(limit)
    } yield news
  }

  private def newsJoinnewsRecommend(channel: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (news, nr) <- newsList.filter(_.channel === channel)
        .join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24))).on(_.nid === _.nid).drop(offset).take(limit)
    } yield (news, nr)
  }
  private val newsJoinnewsRecommendCompiled = Compiled(newsJoinnewsRecommend _)
  private val newsFilternewsRecommendCompiled = Compiled(newsFilternewsRecommend _)

  def listNewsByRecommand(channel: Long, ifrecommend: Int, offset: Long, limit: Long): Future[Seq[NewsRecommendResponse]] = {
    if (ifrecommend == 1) {
      for (pairs <- db.run(newsJoinnewsRecommendCompiled(channel, offset, limit).result)) yield {
        for (pair <- pairs) yield {
          pair match {
            case (newsRow, newsRecommend) => NewsRecommendResponse.from(NewsFeedResponse.from(newsRow), newsRecommend)
          }
        }
      }
    } else {
      for (newsRows <- db.run(newsFilternewsRecommendCompiled(channel, offset, limit).result)) yield {
        for (newsRow <- newsRows) yield {
          newsRow match {
            case newsRow => NewsRecommendResponse.from(NewsFeedResponse.from(newsRow))
          }
        }
      }
    }
  }

  def listNewsByRecommandCount(channel: Long, ifrecommend: Int): Future[Int] = {
    if (ifrecommend == 1) {
      db.run(newsList.filter(_.channel === channel).join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24))).on(_.nid === _.nid).length.result)
    } else {
      db.run(newsList.filter(_.channel === channel).filter(_.ctime > LocalDateTime.now().plusDays(-7)).filterNot(_.nid in newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24)).map(_.nid)).length.result)
    }
  }

  def listNewsBySearch(nids: Seq[Long]): Future[Seq[NewsRecommend]] = {
    db.run(newsRecommendList.filter(_.nid inSet nids).result)
  }

}