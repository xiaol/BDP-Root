package dao.users

import javax.inject.{ Inject, Singleton }

import commons.models.users.UserTopic
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangsl on 2016-11-16.
 *
 */

trait UserTopicTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class UserTopicTable(tag: Tag) extends Table[UserTopic](tag, "user_topic") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def uid = column[Long]("uid")
    def begin_time = column[LocalDateTime]("begin_time")
    def last_time = column[LocalDateTime]("last_time")
    def keywords = column[List[String]]("keywords")
    def failure_time = column[LocalDateTime]("failure_time")
    def ctime = column[LocalDateTime]("ctime")

    def * = (id.?, uid, begin_time, last_time, keywords, failure_time, ctime) <> ((UserTopic.apply _).tupled, UserTopic.unapply)
  }
}

@Singleton
class UserTopicDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserTopicTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val userTopicList = TableQuery[UserTopicTable]

  def findByUid(uid: Long): Future[Seq[UserTopic]] = {
    db.run(userTopicList.filter(_.uid === uid).filter(_.failure_time > LocalDateTime.now()).result)
  }
}
