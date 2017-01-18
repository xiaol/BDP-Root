package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsFeedResponse
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-09.
 *
 */

trait NewsResponseTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsResponseTable(tag: Tag) extends Table[NewsFeedResponse](tag, "newslist_v2") {
    def nid = column[Long]("nid")
    def docid = column[String]("docid")
    def title = column[String]("title")
    def ptime = column[LocalDateTime]("ctime")
    def pname = column[Option[String]]("pname")
    def purl = column[Option[String]]("purl")
    def descr = column[Option[String]]("descr")
    def channel = column[Long]("chid")

    def collect = column[Int]("collect")
    def concern = column[Int]("concern")
    def comment = column[Int]("comment")

    def style = column[Int]("style")
    def imgs = column[Option[List[String]]]("imgs")
    def tags = column[Option[List[String]]]("tags")

    //    def icon = column[Option[String]]("icon")
    //    def videourl = column[Option[String]]("videourl")
    //    def thumbnail = column[Option[String]]("thumbnail")
    //    def duration = column[Option[Int]]("duration")

    def * = (nid, docid, title, ptime, pname, purl, descr, channel, collect, concern, comment, style, imgs, tags, None, None, None, None, None, None) <> ((NewsFeedResponse.apply _).tupled, NewsFeedResponse.unapply)
  }
}

@Singleton
class NewsResponseDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends NewsResponseTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  lazy val newsResponseList = TableQuery[NewsResponseTable]

  def news(): Future[Seq[(Long, String, Option[String])]] = {
    val tablename = "newslist_v" + 2 % 10
    println(tablename)
    val action = sql"select nid, title, imgs from #$tablename limit 10 ".as[(Long, String, Option[String])]
    db.run(action)
  }

}
