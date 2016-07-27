package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news._
import dao.userprofiles.ConcernPublisherTable
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhangshl on 16/7/15.
 */
trait NewsRecommendTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsRecommendTable(tag: Tag) extends Table[NewsRecommend](tag, "newsrecommendlist") {

    import commons.models.news._

    def nid = column[Long]("nid")
    def rtime = column[Option[LocalDateTime]]("rtime")
    def level = column[Option[Double]]("level")
    def bigimg = column[Option[Int]]("bigimg")
    def status = column[Option[Int]]("status")

    def * = (nid, rtime, level, bigimg, status) <> ((NewsRecommend.apply _).tupled, NewsRecommend.unapply)
  }
}

trait NewsRecommendAPPTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsRecommendAPPTable(tag: Tag) extends Table[NewsRecommend](tag, "view_newsrecommendlist") {

    import commons.models.news._

    def nid = column[Long]("nid")
    def rtime = column[Option[LocalDateTime]]("rtime")
    def level = column[Option[Double]]("level")
    def bigimg = column[Option[Int]]("bigimg")
    def status = column[Option[Int]]("status")

    def * = (nid, rtime, level, bigimg, status) <> ((NewsRecommend.apply _).tupled, NewsRecommend.unapply)
  }
}

object NewsRecommendDAO {
  final private val newstimeWindow: Int = -7 //只从近7天的新闻中取
  final private val newsrecommendtimeWindow: Int = -24 //只取近24小时推荐的新闻
}

@Singleton
class NewsRecommendDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsRecommendTable with NewsTable with NewsRecommendAPPTable with NewsRecommendReadTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._
  import NewsRecommendDAO._

  val newsRecommendList = TableQuery[NewsRecommendTable]
  val newsRecommendAPPList = TableQuery[NewsRecommendAPPTable]
  val newsRecommendReadList = TableQuery[NewsRecommendReadTable]
  val newsList = TableQuery[NewsTable]
  val concernPubList = TableQuery[ConcernPublisherTable]
  val publisherList = TableQuery[NewsPublisherTable]

  def insert(newsRecommend: NewsRecommend): Future[Long] = {
    db.run(newsRecommendList returning newsRecommendList.map(_.nid) += newsRecommend)
  }

  def delete(nid: Long): Future[Option[Long]] = {
    db.run(newsRecommendList.filter(_.nid === nid).delete.map {
      case 0 => None
      case _ => Some(nid)
    })
  }

  private def newsFilternewsRecommend(channel: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      news <- newsList.filter(_.chid === channel).filter(_.ctime > LocalDateTime.now().plusDays(-7)).filterNot(_.nid in newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24)).map(_.nid)).sortBy(_.ctime.desc).drop(offset).take(limit)
    } yield news
  }

  private def newsJoinnewsRecommend(channel: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (news, nr) <- newsList.filter(_.chid === channel)
        .join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24))).on(_.nid === _.nid).drop(offset).take(limit)
    } yield (news, nr)
  }
  private val newsJoinnewsRecommendCompiled = Compiled(newsJoinnewsRecommend _)
  private val newsFilternewsRecommendCompiled = Compiled(newsFilternewsRecommend _)

  def listNewsByRecommand(channel: Long, ifrecommend: Int, offset: Long, limit: Long): Future[Seq[NewsRecommendResponse]] = {
    if (ifrecommend == 1) {
      for (pairs <- db.run(newsJoinnewsRecommendCompiled(channel, offset, limit).result)) yield {
        for (pair <- pairs) yield {
          pair match {
            case (newsRow, newsRecommend) => NewsRecommendResponse.from(NewsFeedResponse.from(newsRow), newsRecommend)
          }
        }
      }
    } else {
      for (newsRows <- db.run(newsFilternewsRecommendCompiled(channel, offset, limit).result)) yield {
        for (newsRow <- newsRows) yield {
          newsRow match {
            case newsRow => NewsRecommendResponse.from(NewsFeedResponse.from(newsRow))
          }
        }
      }
    }
  }

  def listNewsByRecommandCount(channel: Long, ifrecommend: Int): Future[Int] = {
    if (ifrecommend == 1) {
      db.run(newsList.filter(_.chid === channel).join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24))).on(_.nid === _.nid).length.result)
    } else {
      db.run(newsList.filter(_.chid === channel).filter(_.ctime > LocalDateTime.now().plusDays(-7)).filterNot(_.nid in newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24)).map(_.nid)).length.result)
    }
  }

  def listNewsBySearch(nids: Seq[Long]): Future[Seq[NewsRecommend]] = {
    db.run(newsRecommendList.filter(_.nid inSet nids).result)
  }

  def listNewsByRecommandUid(uid: Long, offset: Long, limit: Long): Future[Seq[(NewsRow, NewsRecommend)]] = {
    val joinQuery = (for {
      (news, newsRecommends) <- newsList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow))
        .join(newsRecommendAPPList.filter(_.rtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid))).on(_.nid === _.nid).drop(offset).take(limit)
    } yield (news, newsRecommends)).map {
      case (n, newsRecommends) =>
        (n, newsRecommends)
    }

    db.run(joinQuery.result)
  }

  def listNewsByRecommandUidBigImg(uid: Long, offset: Long, limit: Long): Future[Seq[(NewsRow, NewsRecommend)]] = {
    val joinQuery = (for {
      (news, newsRecommends) <- newsList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow))
        .join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).filter(_.bigimg > 0).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid))).on(_.nid === _.nid).sortBy(_._2.level.desc).drop(offset).take(limit)
    } yield (news, newsRecommends)).map {
      case (n, newsRecommends) =>
        (n, newsRecommends)
    }

    db.run(joinQuery.result)
  }
  
  //根据关键字搜索所有订阅号及该用户是否订阅
  def listPublisherWithFlag(uid: Option[Long], keywords: String): Future[Seq[(NewsPublisherRow, Long)]] = {
    //有uid,则查询用用户是否关联了订阅号
    if (uid.isDefined) {
      val joinQuery = (for {
        (np, cp) <- publisherList.filter(_.name like "%" + keywords + "%").joinLeft(concernPubList.filter(_.uid === uid).filter(_.pname like "%" + keywords + "%")).on(_.name === _.pname).sortBy(_._1.concern.desc)
      } yield (np, cp.map(_.id))).map {
        case (np, cp) =>
          (np, cp.getOrElse(0L))
      }
      db.run(joinQuery.result)
    } else {
      val joinQuery = (for {
        np <- publisherList.filter(_.name like "%" + keywords + "%")
      } yield (np)).map {
        case (np) =>
          (np, 0L)
      }
      db.run(joinQuery.result)
    }

  }
}