package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.TopicNews
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangsl on 2016-10-17.
 *
 */

trait TopicNewsTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class TopicNewsTable(tag: Tag) extends Table[TopicNews](tag, "topicnews") {
    def id = column[Int]("id")
    def topic = column[Int]("topic")
    def topic_class = column[Int]("topic_class")
    def news = column[Long]("news")
    def user = column[Int]("user")
    def create_time = column[Option[LocalDateTime]]("create_time")

    def * = (id, topic, topic_class, news, user, create_time) <> ((TopicNews.apply _).tupled, TopicNews.unapply)
  }
}

@Singleton
class TopicNewsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends TopicNewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val topicNews = TableQuery[TopicNewsTable]

}