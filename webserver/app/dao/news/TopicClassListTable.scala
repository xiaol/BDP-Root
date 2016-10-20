package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.TopicClassList
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangsl on 2016-10-17.
 *
 */

trait TopicClassListTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class TopicClassListTable(tag: Tag) extends Table[TopicClassList](tag, "topicclasslist") {
    def id = column[Int]("id")
    def name = column[String]("name")
    def topic = column[Int]("topic")
    def order = column[Option[Int]]("order")

    def * = (id, name, topic, order) <> ((TopicClassList.apply _).tupled, TopicClassList.unapply)
  }
}

@Singleton
class TopicClassListDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends TopicClassListTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val topicClassList = TableQuery[TopicClassListTable]

  def topicDetail(tid: Int): Future[Seq[(TopicClassList)]] = {
    db.run(topicClassList.filter(_.topic === tid).result)
  }

}