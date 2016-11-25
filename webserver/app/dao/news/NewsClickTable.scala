package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsClick
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-09.
 *
 */

trait NewsClickTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsClickTable(tag: Tag) extends Table[NewsClick](tag, "newsrecommendclick") {
    def uid = column[Long]("uid")
    def nid = column[Long]("nid")
    def ctime = column[LocalDateTime]("ctime")

    def * = (uid, nid, ctime) <> ((NewsClick.apply _).tupled, NewsClick.unapply)
  }
}
