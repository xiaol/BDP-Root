package dao.news

import javax.inject.{ Inject, Singleton }

import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json.JsValue
import commons.models.news._
import commons.utils.JodaOderingImplicits
import dao.userprofiles._

/**
 * Created by zhange on 2016-05-10.
 *
 */

trait NewsTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsTable(tag: Tag) extends Table[NewsRow](tag, "newslist_v2") {

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
    //    def icon = column[Option[String]]("icon")

    def collect = column[Int]("collect")
    def concern = column[Int]("concern")
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
    //    def videotype = column[Option[Int]]("videotype")

    def base = (nid.?, url, docid, title, content, html, author, ptime, pname, purl, descr, tags, province, city, district) <> ((NewsRowBase.apply _).tupled, NewsRowBase.unapply)
    def incr = (collect, concern, comment, inum, style, imgs, compress, ners) <> ((NewsRowIncr.apply _).tupled, NewsRowIncr.unapply)
    def syst = (state, ctime, chid, sechid, srid, srstate, pconf, plog) <> ((NewsRowSyst.apply _).tupled, NewsRowSyst.unapply)
    //    def syst = (state, ctime, chid, sechid, srid, srstate, pconf, plog, videotype) <> ((NewsRowSyst.apply _).tupled, NewsRowSyst.unapply)
    def * = (base, incr, syst) <> ((NewsRow.apply _).tupled, NewsRow.unapply)
  }
}

object NewsDAO {
  final private val timeWindow = (timeCursor: LocalDateTime) => timeCursor.plusDays(-7)

  final private val shieldedCid: Long = 28L // 本地频道
}

@Singleton
class NewsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsTable with CollectTable with ConcernTable with ConcernPublisherTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._
  import NewsDAO._

  type NewsTableQuery = Query[NewsTable, NewsTable#TableElementType, Seq]

  val newsList = TableQuery[NewsTable]
  val collectList = TableQuery[CollectTable]
  val concernList = TableQuery[ConcernTable]
  val concernPublisherList = TableQuery[ConcernPublisherTable]

  def findByNid(nid: Long): Future[Option[NewsRow]] = {
    db.run(newsList.filter(_.nid === nid).result.headOption)
  }

  // TODO: join newspublisherlist
  def findByNidWithProfile(nid: Long, uid: Long): Future[Option[(NewsRow, Int, Int, Int)]] = {
    val joinQuery = (for {
      (((news, collects), concerns), conpubs) <- newsList.filter(_.nid === nid)
        .joinLeft(collectList.filter(_.uid === uid)).on(_.nid === _.nid)
        .joinLeft(concernList.filter(_.uid === uid)).on(_._1.nid === _.nid)
        .joinLeft(concernPublisherList.filter(_.uid === uid)).on(_._1._1.pname === _.pname)
    } yield (news, collects.map(_.id), concerns.map(_.id), conpubs.map(_.id))).groupBy(_._1).map {
      case (n, joins) =>
        (n, joins.map(_._2).countDefined, joins.map(_._3).countDefined, joins.map(_._4).countDefined)
    }

    db.run(joinQuery.result.headOption)
  }

  def findByDocid(docid: String): Future[Option[NewsRow]] = {
    db.run(newsList.filter(_.docid === docid).result.headOption)
  }

  def insert(newsRow: NewsRow): Future[Long] = {
    db.run(newsList returning newsList.map(_.nid) += newsRow)
  }

  def update(nid: Long, newsRow: NewsRow): Future[Option[NewsRow]] = {
    db.run(newsList.filter(_.nid === nid).update(newsRow).map {
      case 0 => None
      case _ => Some(newsRow)
    })
  }

  def delete(nid: Long): Future[Option[Long]] = {
    db.run(newsList.filter(_.nid === nid).delete.map {
      case 0 => None
      case _ => Some(nid)
    })
  }

  def loadByHot(offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.ctime > timeWindow(timeCursor)).filter(_.ctime < timeCursor).filter(_.comment > 0).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }
  def loadByCold(offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.ctime > timeWindow(timeCursor)).filter(_.ctime < timeCursor).filter(_.comment === 0).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def refreshByHot(offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.ctime > timeCursor).filter(_.comment > 0).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }
  def refreshByCold(offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.ctime > timeCursor).filter(_.comment === 0).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def loadByChannel(chid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid === chid).filter(_.ctime > timeWindow(timeCursor)).filter(_.ctime < timeCursor).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def refreshByChannel(chid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid === chid).filter(_.ctime > timeCursor).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def loadBySeChannel(chid: Long, sechid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid === chid).filter(_.sechid === sechid).filter(_.ctime < timeCursor).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def refreshBySeChannel(chid: Long, sechid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid === chid).filter(_.sechid === sechid).filter(_.ctime > timeCursor).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def loadByLocation(offset: Long, limit: Long, timeCursor: LocalDateTime, province: Option[String], city: Option[String], district: Option[String]): Future[Seq[NewsRow]] = {
    val newsListWithLocation = newsListWithOptionalLocation(province, city, district)
    db.run(newsListWithLocation.filter(_.ctime > timeWindow(timeCursor)).filter(_.ctime < timeCursor).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def refreshByLocation(offset: Long, limit: Long, timeCursor: LocalDateTime, province: Option[String], city: Option[String], district: Option[String]): Future[Seq[NewsRow]] = {
    val newsListWithLocation = newsListWithOptionalLocation(province, city, district)
    db.run(newsListWithLocation.filter(_.ctime > timeCursor).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def loadBySource(srid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.srid === srid).filter(_.ctime > timeWindow(timeCursor)).filter(_.ctime < timeCursor).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def refreshBySource(srid: Long, offset: Long, limit: Long, timeCursor: LocalDateTime): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.srid === srid).filter(_.ctime > timeCursor).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  private def newsListWithOptionalLocation(province: Option[String], city: Option[String], district: Option[String]) = {
    val provinceFilter = province match {
      case Some(p) => newsList.filter(_.province === p)
      case None    => newsList
    }
    val cityFilter = city match {
      case Some(c) => provinceFilter.filter(_.city === c)
      case None    => provinceFilter
    }
    district match {
      case Some(d) => cityFilter.filter(_.district === d)
      case None    => cityFilter
    }
  }

  def updateCollect(nid: Long, collect: Int): Future[Option[Int]] = {
    val queryCollect = newsList.filter(_.nid === nid).map(_.collect)
    val collectOpt: Future[Option[Int]] = db.run(queryCollect.result.headOption)
    collectOpt.flatMap {
      case Some(c) => db.run(queryCollect.update(c + collect).map {
        case 0 => None
        case _ => Some(c + collect)
      })
      case _ => Future.successful(None)
    }
  }

  def updateConcern(nid: Long, concern: Int): Future[Option[Int]] = {
    val queryConcern = newsList.filter(_.nid === nid).map(_.concern)
    val concernOpt: Future[Option[Int]] = db.run(queryConcern.result.headOption)
    concernOpt.flatMap {
      case Some(c) => db.run(queryConcern.update(c + concern).map {
        case 0 => None
        case _ => Some(c + concern)
      })
      case _ => Future.successful(None)
    }
  }

  def updateComment(docid: String, comment: Int): Future[Option[Int]] = {
    val queryComment = newsList.filter(_.docid === docid).map(_.comment)
    val commentOpt: Future[Option[Int]] = db.run(queryComment.result.headOption)
    commentOpt.flatMap {
      case Some(c) => db.run(queryComment.update(c + comment).map {
        case 0 => None
        case _ => Some(c + comment)
      })
      case _ => Future.successful(None)
    }
  }
}