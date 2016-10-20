package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.TopicNewsRead
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangsl on 2016-10-17.
 *
 */

trait TopicNewsReadTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class TopicNewsReadTable(tag: Tag) extends Table[TopicNewsRead](tag, "topicnewsread") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def uid = column[Long]("uid")
    def nid = column[Long]("nid")
    def cid = column[Int]("cid")
    def tid = column[Int]("tid")
    def ctime = column[LocalDateTime]("ctime")

    def * = (id.?, uid, nid, cid, tid, ctime) <> ((TopicNewsRead.apply _).tupled, TopicNewsRead.unapply)
  }
}

@Singleton
class TopicNewsReadDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends TopicNewsReadTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val topicNewsReadList = TableQuery[TopicNewsReadTable]

  def insert(topicNewsReadBean: TopicNewsRead): Future[Long] = {
    db.run(topicNewsReadList returning topicNewsReadList.map(_.id) += topicNewsReadBean)
  }

  def insertByTid(uid: Long, tid: Int) = {
    val action = sqlu"INSERT INTO topicnewsread(uid, tid, cid, nid, ctime) select $uid,topic,topic_class,news,now() from topicnews t where not exists (select 1 from topicnewsread r where t.topic=r.tid and t.topic_class=r.cid and t.news=r.nid and r.uid=$uid) and topic=$tid "
    db.run(action)
  }

}