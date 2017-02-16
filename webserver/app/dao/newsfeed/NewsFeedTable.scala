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

  //  def news(): Future[Seq[(Long, String, Option[String])]] = {
  //    val tablename = "newslist_v" + 2 % 10
  //    val action = sql"select nid, title, imgs from #$tablename where imgs is not null limit 10 ".as[(Long, String, Option[String])]
  //    db.run(action)
  //  }

  def news(uid: Long): Future[Seq[(Long, String, Option[String])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql"select * from(select nid, title from newslist_v2 nv where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=#$uid and nr.readtime>now()-interval'3 day') and nv.ctime>now()-interval'3 day' and nv.comment>0 and nv.chid != 28 and nv.state=0 and nv.pname not in('就是逗你笑', 'bomb01') order by nv.comment desc limit 1)hot1 ".as[(Long, String, Option[String])]
    db.run(action)
  }

  def insertRead(newsRecommendRead: Seq[NewsRecommendRead]) = {
    val tablename: String = "newsrecommendread_" + newsRecommendRead.head.uid % 100
    val values = newsRecommendRead.map { read => "(" + read.uid + ", " + read.nid + ", now())" }.mkString(",")
    val action = sqlu""" INSERT INTO #$tablename (uid, nid, readtime) VALUES #$values """
    db.run(action)
  }

}
