package dao.newsfeed

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsRecommendRead
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class NewsFeedDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  def insertRead(newsRecommendRead: Seq[NewsRecommendRead]) = {
    val tablename: String = "newsrecommendread_" + newsRecommendRead.head.uid % 100
    val values = newsRecommendRead.map { read => "(" + read.uid + ", " + read.nid + ", now(), " + read.logtype.getOrElse(0) + ", " + read.logchid.getOrElse(1) + ")" }.mkString(",")
    val action = sqlu""" INSERT INTO #$tablename (uid, nid, readtime, logtype, logchid) VALUES #$values """
    db.run(action)
  }

}
