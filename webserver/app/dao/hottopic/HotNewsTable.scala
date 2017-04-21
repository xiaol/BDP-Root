package dao.hottopic

import javax.inject.{ Inject, Singleton }

import commons.models.hottopic.HotNews
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

trait HotNewsTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class HotTopicTable(tag: Tag) extends Table[HotNews](tag, "newsrecommendhot") {

    def nid = column[Long]("nid")
    def ctime = column[LocalDateTime]("ctime")
    def status = column[Int]("status")
    def source = column[String]("hotword")

    def * = (nid, ctime, status, source) <> ((HotNews.apply _).tupled, HotNews.unapply)
  }
}

@Singleton
class HotNewsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HotNewsTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val hotNewsList = TableQuery[HotTopicTable]

  def insert(hotNews: HotNews): Future[Long] = {
    db.run(hotNewsList returning hotNewsList.map(_.nid) += hotNews)
  }

  def insertAll(hotNews: Seq[HotNews]): Future[Seq[Long]] = {
    db.run(hotNewsList returning hotNewsList.map(_.nid) ++= hotNews)
  }
}