package dao.video

import javax.inject.{ Inject, Singleton }

import commons.models.video._
import dao.news.NewsRecommendReadTable
import dao.userprofiles.{ CollectTable, ConcernPublisherTable, ConcernTable }
import org.joda.time._
import play.api.db.slick._
import play.api.libs.json.JsValue
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

trait VideoTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class VideoTable(tag: Tag) extends Table[VideoRow](tag, "newslist_v2") {

    def nid = column[Long]("nid", O.PrimaryKey, O.AutoInc)
    def url = column[String]("url")
    def docid = column[String]("docid")
    def title = column[String]("title")
    def content = column[JsValue]("content")
    def html = column[String]("html")
    def author = column[Option[String]]("author")
    def ptime = column[LocalDateTime]("ptime")
    def pname = column[Option[String]]("pname")
    def purl = column[Option[String]]("purl")
    def descr = column[Option[String]]("descr")
    def tags = column[Option[List[String]]]("tags")
    def province = column[Option[String]]("province")
    def city = column[Option[String]]("city")
    def district = column[Option[String]]("district")

    def collect = column[Int]("collect")
    def concern = column[Int]("concern")
    def un_concern = column[Option[Int]]("un_concern")
    def comment = column[Int]("comment")
    def inum = column[Int]("inum")
    def style = column[Int]("style")
    def imgs = column[Option[List[String]]]("imgs")
    def compress = column[Option[String]]("compress")
    def ners = column[Option[JsValue]]("ners")

    def state = column[Int]("state")
    def ctime = column[LocalDateTime]("ctime")
    def chid = column[Long]("chid")
    def srid = column[Long]("srid")
    def srstate = column[Int]("srstate")
    def pconf = column[Option[JsValue]]("pconf")
    def plog = column[Option[JsValue]]("plog")
    def sechid = column[Option[Long]]("sechid")
    def icon = column[Option[String]]("icon")
    def videourl = column[Option[String]]("videourl")
    def thumbnail = column[Option[String]]("thumbnail")
    def duration = column[Option[Int]]("duration")
    def rtype = column[Option[Int]]("rtype")

    def base = (nid.?, url, docid, title, content, html, author, ptime, pname, purl, descr, tags, province, city, district) <> ((VideoRowBase.apply _).tupled, VideoRowBase.unapply)
    def incr = (collect, concern, un_concern, comment, inum, style, imgs, compress, ners) <> ((VideoRowIncr.apply _).tupled, VideoRowIncr.unapply)
    def syst = (state, ctime, chid, sechid, srid, srstate, pconf, plog, icon, videourl, thumbnail, duration, rtype) <> ((VideoRowSyst.apply _).tupled, VideoRowSyst.unapply)
    def * = (base, incr, syst) <> ((VideoRow.apply _).tupled, VideoRow.unapply)
  }
}

object VideoDAO {
  final private val timeWindow1 = (timeCursor: LocalDateTime) => timeCursor.plusDays(-1)
  final private val timeWindow3 = (timeCursor: LocalDateTime) => timeCursor.plusDays(-3)
}

@Singleton
class VideoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends VideoTable with CollectTable
    with ConcernTable with ConcernPublisherTable with NewsRecommendReadTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import VideoDAO._
  import driver.api._

  type VideoTableQuery = Query[VideoTable, VideoTable#TableElementType, Seq]

  val videoList = TableQuery[VideoTable].filter(_.rtype === 6)
  val collectList = TableQuery[CollectTable]
  val concernList = TableQuery[ConcernTable]
  val concernPublisherList = TableQuery[ConcernPublisherTable]
  val newsRecommendReadList = TableQuery[NewsRecommendReadTable]

  def refreshVideoByChannel(uid: Long, chid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime, nid: Option[Long]): Future[Seq[VideoRow]] = {
    db.run(videoList.filter(_.state === 0).filter(_.ctime > timeWindow3(timeCursor)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > timeWindow3(timeCursor)).map(_.nid)).sortBy(_.nid.desc).drop(offset).take(limit).result)
  }

  def loadVideoByChannel(uid: Long, chid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime, nid: Option[Long]): Future[Seq[VideoRow]] = {
    db.run(videoList.filter(_.state === 0).filter(_.ctime > timeWindow3(timeCursor)).filter(_.ctime < timeCursor).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > timeWindow3(timeCursor)).map(_.nid)).sortBy(_.nid.desc).drop(offset).take(limit).result)
  }

  def findByNid(nid: Long): Future[Option[VideoRow]] = {
    db.run(videoList.filter(_.nid === nid).result.map(_.headOption))
  }

  // TODO: join newspublisherlist
  def findByNidWithProfile(nid: Long, uid: Long): Future[Option[(VideoRow, Int, Int, Int)]] = {
    val joinQuery = (for {
      (((news, collects), concerns), conpubs) <- videoList.filter(_.nid === nid)
        .joinLeft(collectList.filter(_.uid === uid)).on(_.nid === _.nid)
        .joinLeft(concernList.filter(_.uid === uid)).on(_._1.nid === _.nid)
        .joinLeft(concernPublisherList.filter(_.uid === uid)).on(_._1._1.pname === _.pname)
    } yield (news, collects.map(_.id), concerns.map(_.id), conpubs.map(_.id))).groupBy(_._1).map {
      case (n, joins) =>
        (n, joins.map(_._2).countDefined, joins.map(_._3).countDefined, joins.map(_._4).countDefined)
    }

    db.run(joinQuery.result.map(_.headOption))
  }

}