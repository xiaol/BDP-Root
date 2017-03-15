package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news._
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.ExecutionContext

/**
 * Created by zhange on 2016-05-10.
 *
 */

trait NewsSimpleTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsSimpleTable(tag: Tag) extends Table[NewsSimpleRow](tag, "newslist_v2") {

    def nid = column[Long]("nid", O.PrimaryKey, O.AutoInc)
    def url = column[String]("url")
    def docid = column[String]("docid")
    def title = column[String]("title")
    def author = column[Option[String]]("author")
    def ptime = column[LocalDateTime]("ptime")
    def pname = column[Option[String]]("pname")
    def purl = column[Option[String]]("purl")
    def descr = column[Option[String]]("descr")
    def tags = column[Option[List[String]]]("tags")

    def collect = column[Int]("collect")
    def concern = column[Int]("concern")
    def comment = column[Int]("comment")
    def inum = column[Int]("inum")
    def style = column[Int]("style")
    def imgs = column[Option[List[String]]]("imgs")

    def state = column[Int]("state")
    def ctime = column[LocalDateTime]("ctime")
    def chid = column[Long]("chid")
    def sechid = column[Option[Long]]("sechid")
    def icon = column[Option[String]]("icon")
    def rtype = column[Option[Int]]("rtype")
    def videourl = column[Option[String]]("videourl")
    def thumbnail = column[Option[String]]("thumbnail")
    def duration = column[Option[Int]]("duration")

    def base = (nid.?, url, docid, title, author, ptime, pname, purl, descr, tags) <> ((NewsSimpleRowBase.apply _).tupled, NewsSimpleRowBase.unapply)
    def incr = (collect, concern, comment, inum, style, imgs) <> ((NewsSimpleRowIncr.apply _).tupled, NewsSimpleRowIncr.unapply)
    def syst = (state, ctime, chid, sechid, icon, rtype, videourl, thumbnail, duration, None, None) <> ((NewsSimpleRowSyst.apply _).tupled, NewsSimpleRowSyst.unapply)
    def * = (base, incr, syst) <> ((NewsSimpleRow.apply _).tupled, NewsSimpleRow.unapply)
  }
}

object NewsSimpleDAO {
  final private val timeWindow = (timeCursor: LocalDateTime) => timeCursor.plusDays(-7)
  final private val dayWindow = LocalDateTime.now().plusDays(-1)
  final private val shieldedCid: Long = 28L // 本地频道
  final private val filterSet = Set("就是逗你笑", "bomb01")
}

@Singleton
class NewsSimpleDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsSimpleTable with HasDatabaseConfigProvider[MyPostgresDriver] {

}