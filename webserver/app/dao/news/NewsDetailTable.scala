package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news._
import dao.userprofiles._
import dao.video.VideoTable
import org.joda.time._
import play.api.db.slick._
import play.api.libs.json.JsValue
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-10.
 *
 */

trait NewsDetailTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsDetailTable(tag: Tag) extends Table[NewsDetailRow](tag, "info_news") {

    def nid = column[Long]("nid", O.PrimaryKey, O.AutoInc)
    def url = column[String]("url")
    def docid = column[String]("docid")
    def title = column[String]("title")
    def content = column[JsValue]("content")
    def author = column[Option[String]]("author")
    def ptime = column[LocalDateTime]("ptime")
    def pname = column[Option[String]]("pname")
    def purl = column[Option[String]]("purl")
    def tags = column[Option[List[String]]]("tags")
    def province = column[Option[String]]("province")
    def city = column[Option[String]]("city")
    def district = column[Option[String]]("district")
    def inum = column[Int]("inum")
    def style = column[Int]("style")
    def imgs = column[Option[List[String]]]("imgs")
    def ctime = column[LocalDateTime]("ctime")
    def chid = column[Long]("chid")
    def srid = column[Long]("srid")
    def sechid = column[Option[Long]]("sechid")
    def icon = column[Option[String]]("icon")

    def base = (nid.?, url, docid, title, content, author, ptime, pname, purl, tags, province, city, district) <> ((NewsDetailRowBase.apply _).tupled, NewsDetailRowBase.unapply)
    def syst = (inum, style, imgs, ctime, chid, sechid, srid, icon) <> ((NewsDetailRowSyst.apply _).tupled, NewsDetailRowSyst.unapply)
    def * = (base, syst) <> ((NewsDetailRow.apply _).tupled, NewsDetailRow.unapply)
  }
}

object NewsDetailDAO {
  final private val dayWindow = LocalDateTime.now().plusDays(-1)
}

@Singleton
class NewsDetailDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsDetailTable with NewsTable with CollectTable with ConcernTable with ConcernPublisherTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._
  import NewsDetailDAO._

  type NewsTableQuery = Query[NewsDetailTable, NewsDetailTable#TableElementType, Seq]

  val newsDetailList = TableQuery[NewsDetailTable]
  val newsFeedList = TableQuery[NewsTable]
  val collectList = TableQuery[CollectTable]
  val concernList = TableQuery[ConcernTable]
  val concernPublisherList = TableQuery[ConcernPublisherTable]

  def findDetailByNid(nid: Long): Future[Option[NewsDetailRow]] = {
    db.run(newsDetailList.filter(_.nid === nid).result.map(_.headOption))
  }
  def findNextDetailByNid(nid: Long, chid: Long): Future[Option[NewsDetailRow]] = {
    db.run(newsDetailList.filter(_.nid < nid).filter(_.ctime > dayWindow).sortBy(_.ctime.desc).take(1).result.map(_.headOption))
  }
  def findLastDetailByNid(nid: Long, chid: Long): Future[Option[NewsDetailRow]] = {
    db.run(newsDetailList.filter(_.nid > nid).filter(_.ctime > dayWindow).sortBy(_.ctime.asc).take(1).result.map(_.headOption))
  }

  def findByNid(nid: Long): Future[Option[(NewsRow, NewsDetailRow)]] = {
    val joinQuery = (for {
      (newsFeed, newsDetail) <- newsFeedList.filter(_.nid === nid).join(newsDetailList.filter(_.nid === nid)).on(_.nid === _.nid)
    } yield (newsFeed, newsDetail))

    db.run(joinQuery.result.map(_.headOption))
  }

  def findNextByNid(nid: Long, chid: Long): Future[Option[(NewsRow, NewsDetailRow)]] = {
    val joinQuery = (for {
      (newsFeed, newsDetail) <- newsFeedList.filter(_.chid === chid).filter(_.ctime > dayWindow).filter(_.nid < nid).sortBy(_.ctime.desc).take(1).join(newsDetailList).on(_.nid === _.nid)
    } yield (newsFeed, newsDetail))

    db.run(joinQuery.result.map(_.headOption))
  }

  def findLastByNid(nid: Long, chid: Long): Future[Option[(NewsRow, NewsDetailRow)]] = {
    val joinQuery = (for {
      (newsFeed, newsDetail) <- newsFeedList.filter(_.chid === chid).filter(_.ctime > dayWindow).filter(_.nid > nid).sortBy(_.ctime.asc).take(1).join(newsDetailList).on(_.nid === _.nid)
    } yield (newsFeed, newsDetail))

    db.run(joinQuery.result.map(_.headOption))
  }

  def findByNidWithProfile(nid: Long, uid: Long): Future[Option[(NewsRow, Int, Int, Int)]] = {
    val joinQuery = (for {
      (((news, collects), concerns), conpubs) <- newsFeedList.filter(_.nid === nid)
        .joinLeft(collectList.filter(_.uid === uid)).on(_.nid === _.nid)
        .joinLeft(concernList.filter(_.uid === uid)).on(_._1.nid === _.nid)
        .joinLeft(concernPublisherList.filter(_.uid === uid)).on(_._1._1.pname === _.pname)
    } yield (news, collects.map(_.id), concerns.map(_.id), conpubs.map(_.id))).groupBy(_._1).map {
      case (n, joins) =>
        (n, joins.map(_._2).countDefined, joins.map(_._3).countDefined, joins.map(_._4).countDefined)
    }

    db.run(joinQuery.result.map(_.headOption))
  }

  def findNextByNidWithProfile(nid: Long, uid: Long, chid: Long): Future[Option[(NewsRow, Int, Int, Int)]] = {
    val joinQuery = (for {
      (((news, collects), concerns), conpubs) <- newsFeedList.filter(_.chid === chid).filter(_.ctime > dayWindow).filter(_.nid < nid).sortBy(_.ctime.desc).take(1)
        .joinLeft(collectList.filter(_.uid === uid)).on(_.nid === _.nid)
        .joinLeft(concernList.filter(_.uid === uid)).on(_._1.nid === _.nid)
        .joinLeft(concernPublisherList.filter(_.uid === uid)).on(_._1._1.pname === _.pname)
    } yield (news, collects.map(_.id), concerns.map(_.id), conpubs.map(_.id))).groupBy(_._1).map {
      case (n, joins) =>
        (n, joins.map(_._2).countDefined, joins.map(_._3).countDefined, joins.map(_._4).countDefined)
    }

    db.run(joinQuery.result.map(_.headOption))
  }

  def findLastByNidWithProfile(nid: Long, uid: Long, chid: Long): Future[Option[(NewsRow, Int, Int, Int)]] = {
    val joinQuery = (for {
      (((news, collects), concerns), conpubs) <- newsFeedList.filter(_.chid === chid).filter(_.ctime > dayWindow).filter(_.nid > nid).sortBy(_.ctime.asc).take(1)
        .joinLeft(collectList.filter(_.uid === uid)).on(_.nid === _.nid)
        .joinLeft(concernList.filter(_.uid === uid)).on(_._1.nid === _.nid)
        .joinLeft(concernPublisherList.filter(_.uid === uid)).on(_._1._1.pname === _.pname)
    } yield (news, collects.map(_.id), concerns.map(_.id), conpubs.map(_.id))).groupBy(_._1).map {
      case (n, joins) =>
        (n, joins.map(_._2).countDefined, joins.map(_._3).countDefined, joins.map(_._4).countDefined)
    }

    db.run(joinQuery.result.map(_.headOption))
  }

  def findByDocid(docid: String): Future[Option[NewsDetailRow]] = {
    db.run(newsDetailList.filter(_.docid === docid).result.map(_.headOption))
  }

}