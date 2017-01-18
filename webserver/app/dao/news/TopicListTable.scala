package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news._
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangsl on 2016-10-17.
 *
 */

trait TopicListTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class TopicListTable(tag: Tag) extends Table[TopicList](tag, "topiclist") {
    def id = column[Int]("id")
    def name = column[String]("name")
    def owner = column[Option[Int]]("owner")
    def cover = column[String]("cover")
    def description = column[String]("description")
    def class_count = column[Int]("class_count")
    def news_count = column[Int]("news_count")
    def online = column[Int]("online")
    def top = column[Int]("top")
    def create_time = column[Option[LocalDateTime]]("create_time")

    def * = (id, name, owner, cover, description, class_count, news_count, online, top, create_time) <> ((TopicList.apply _).tupled, TopicList.unapply)
  }
}

object TopicListDAO {
  final private val newstimeWindow: Int = -7
  final private val newsrecommendtimeWindow: Int = -24
}

@Singleton
class TopicListDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends TopicListTable with TopicClassListTable with TopicNewsTable with NewsTable with TopicNewsReadTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._
  import TopicListDAO._

  val topicList = TableQuery[TopicListTable]
  val topicClassList = TableQuery[TopicClassListTable]
  val topicNews = TableQuery[TopicNewsTable]
  val newsList = TableQuery[NewsTable]
  val topicNewsRead = TableQuery[TopicNewsReadTable]

  def topicDetail(tid: Int): Future[Seq[(TopicList, TopicClassList, TopicNews, NewsRow)]] = {
    db.run(topicClassNewsCompiled(tid).result)
  }

  private val topicClassNewsCompiled = Compiled(topicClassNews _)

  private def topicClassNews(tid: ConstColumn[Int]) = {
    for {
      (topic, news) <- topicList.filter(_.id === tid).filter(_.online === 1).join(topicClassList).on(_.id === _.topic).join(topicNews).on(_._2.id === _.topic_class).join(newsList).on(_._2.news === _.nid).sortBy(p => (p._1._1._2.order.desc, p._1._2.order.desc))
    } yield (topic._1._1, topic._1._2, topic._2, news)
  }

  def topicShow(uid: Long): Future[Seq[(TopicList)]] = {
    db.run(topicList.filter(_.online === 1).filter(_.top === 1).unionAll(topicList.filter(_.online === 1).filter(_.top =!= 1).filter(_.id in (topicNews.filter(_.topic in (topicList.filter(_.online === 1).filter(_.create_time > LocalDateTime.now().plusDays(-1)).map(_.id))).filterNot(_.news in (topicNewsRead.filter(_.uid === uid).filter(_.ctime > LocalDateTime.now().plusDays(-1)).map(_.nid))).map(_.topic)))).result)
  }

}