package dao.video

import javax.inject.{ Inject, Singleton }

import commons.models.video.{ VideoDetailRow, VideoDetailRowBase, VideoDetailRowSyst }
import org.joda.time._
import play.api.db.slick._
import play.api.libs.json.JsValue
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-10.
 *
 */

trait VideoDetailTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class VideoDetailTable(tag: Tag) extends Table[VideoDetailRow](tag, "info_video") {

    def nid = column[Long]("nid")
    def url = column[String]("url")
    def docid = column[String]("docid")
    def title = column[String]("title")
    def content = column[Option[JsValue]]("content")
    def author = column[Option[String]]("author")
    def ptime = column[LocalDateTime]("ptime")
    def pname = column[Option[String]]("pname")
    def tags = column[Option[List[String]]]("tags")

    def style = column[Int]("style")
    def imgs = column[Option[List[String]]]("imgs")
    def ctime = column[LocalDateTime]("ctime")
    def chid = column[Long]("chid")
    def sechid = column[Option[Long]]("sechid")
    def srid = column[Option[Long]]("srid")
    def icon = column[Option[String]]("icon")
    def videourl = column[Option[String]]("videourl")
    def duration = column[Option[Int]]("duration")

    def base = (nid, url, docid, title, content, author, ptime, pname, tags) <> ((VideoDetailRowBase.apply _).tupled, VideoDetailRowBase.unapply)
    def syst = (style, imgs, ctime, chid, sechid, srid, icon, videourl, duration) <> ((VideoDetailRowSyst.apply _).tupled, VideoDetailRowSyst.unapply)
    def * = (base, syst) <> ((VideoDetailRow.apply _).tupled, VideoDetailRow.unapply)
  }
}

@Singleton
class VideoDetailDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends VideoDetailTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val videoDetailList = TableQuery[VideoDetailTable]

  def findDetailByNid(nid: Long): Future[Option[VideoDetailRow]] = {
    db.run(videoDetailList.filter(_.nid === nid).result.map(_.headOption))
  }

  def findByDocid(docid: String): Future[Option[VideoDetailRow]] = {
    db.run(videoDetailList.filter(_.docid === docid).result.map(_.headOption))
  }

}