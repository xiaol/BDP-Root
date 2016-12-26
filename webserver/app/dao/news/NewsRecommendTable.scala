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
  final private val recommendtimeWindow: Int = -3 //只从近3天系统推荐的新闻
  final private val newsrecommendtimeWindow: Int = -24 //只取近24小时人工推荐的新闻
  final private val newsWindow: Int = -6 //没新闻的时候,主要是6小时内没看过的新闻都可以
  final private val shieldedCid: Long = 28L // 本地频道
  final private val filterSet = Set("就是逗你笑", "bomb01")
}

@Singleton
class NewsRecommendDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends NewsRecommendTable with NewsTable with NewsRecommendAPPTable with NewsRecommendReadTable with NewsRecommendForUserTable with NewsRecommendHotTable with ConcernPublisherTable with NewsPublisherTable with NewsClickTable with NewsRecommendLikeTable
    with HasDatabaseConfigProvider[MyPostgresDriver] {

  import driver.api._
  import NewsRecommendDAO._

  val newsRecommendList = TableQuery[NewsRecommendTable]
  val newsRecommendAPPList = TableQuery[NewsRecommendAPPTable]
  val newsRecommendReadList = TableQuery[NewsRecommendReadTable]
  val newsRecommendForUserList = TableQuery[NewsRecommendForUserTable]
  val newsRecommendHotList = TableQuery[NewsRecommendHotTable]
  val newsList = TableQuery[NewsTable]
  val concernPubList = TableQuery[ConcernPublisherTable]
  val publisherList = TableQuery[NewsPublisherTable]
  val newsClickList = TableQuery[NewsClickTable]
  val newsRecommendLikeList = TableQuery[NewsRecommendLikeTable]

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

  private def newsFilternewsRecommendAll(offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      news <- newsList.filter(_.ctime > LocalDateTime.now().plusDays(-7)).filterNot(_.nid in newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24)).map(_.nid)).sortBy(_.ctime.desc).drop(offset).take(limit)
    } yield news
  }

  private def newsJoinnewsRecommend(channel: ConstColumn[Long], offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (news, nr) <- newsList.filter(_.chid === channel)
        .join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24))).on(_.nid === _.nid).sortBy(_._2.rtime.desc).drop(offset).take(limit)
    } yield (news, nr)
  }

  private def newsJoinnewsRecommendAll(offset: ConstColumn[Long], limit: ConstColumn[Long]) = {
    for {
      (news, nr) <- newsList.join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24))).on(_.nid === _.nid).sortBy(_._2.rtime.desc).drop(offset).take(limit)
    } yield (news, nr)
  }

  private val newsJoinnewsRecommendCompiled = Compiled(newsJoinnewsRecommend _)
  private val newsJoinnewsRecommendAllCompiled = Compiled(newsJoinnewsRecommendAll _)
  private val newsFilternewsRecommendCompiled = Compiled(newsFilternewsRecommend _)
  private val newsFilternewsRecommendAllCompiled = Compiled(newsFilternewsRecommendAll _)

  def listNewsByRecommand(channel: Option[Long], ifrecommend: Int, offset: Long, limit: Long): Future[Seq[NewsRecommendResponse]] = {
    if (ifrecommend == 1) {
      //查询已推荐
      if (channel.isDefined) {
        //按频道查询已推荐
        for (pairs <- db.run(newsJoinnewsRecommendCompiled(channel.get, offset, limit).result)) yield {
          for (pair <- pairs) yield {
            pair match {
              case (newsRow, newsRecommend) => NewsRecommendResponse.from(NewsFeedResponse.from(newsRow), newsRecommend)
            }
          }
        }
      } else {
        //查询所有已推荐
        for (pairs <- db.run(newsJoinnewsRecommendAllCompiled(offset, limit).result)) yield {
          for (pair <- pairs) yield {
            pair match {
              case (newsRow, newsRecommend) => NewsRecommendResponse.from(NewsFeedResponse.from(newsRow), newsRecommend)
            }
          }
        }
      }
    } else {
      if (channel.isDefined) {
        for (newsRows <- db.run(newsFilternewsRecommendCompiled(channel.get, offset, limit).result)) yield {
          for (newsRow <- newsRows) yield {
            newsRow match {
              case newsRow => NewsRecommendResponse.from(NewsFeedResponse.from(newsRow))
            }
          }
        }
      } else {
        for (newsRows <- db.run(newsFilternewsRecommendAllCompiled(offset, limit).result)) yield {
          for (newsRow <- newsRows) yield {
            newsRow match {
              case newsRow => NewsRecommendResponse.from(NewsFeedResponse.from(newsRow))
            }
          }
        }
      }

    }
  }

  def listNewsByRecommandCount(channel: Option[Long], ifrecommend: Int): Future[Int] = {
    val newsListFilter = channel match {
      case Some(c) => newsList.filter(_.chid === c)
      case None    => newsList
    }
    if (ifrecommend == 1) {
      db.run(newsListFilter.join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24))).on(_.nid === _.nid).length.result)
    } else {
      db.run(newsListFilter.filter(_.ctime > LocalDateTime.now().plusDays(-7)).filterNot(_.nid in newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(-24)).map(_.nid)).length.result)
    }
  }

  def listNewsBySearch(nids: Seq[Long]): Future[Seq[NewsRecommend]] = {
    db.run(newsRecommendList.filter(_.nid inSet nids).result)
  }

  def listNewsByRecommandUid(uid: Long, offset: Long, limit: Long): Future[Seq[(NewsRow, NewsRecommend)]] = {
    val joinQuery = (for {
      (news, newsRecommends) <- newsList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow))
        .join(newsRecommendAPPList.filter(_.rtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid))).on(_.nid === _.nid).sortBy(p => (p._2.level.desc, p._2.rtime.desc)).drop(offset).take(limit)
    } yield (news, newsRecommends)).map {
      case (n, newsRecommends) =>
        (n, newsRecommends)
    }

    db.run(joinQuery.result)
  }

  //必须出,等级为5大图
  def listNewsByRecommandUidBigImg5(uid: Long, offset: Long, limit: Long): Future[Seq[(NewsRow, NewsRecommend)]] = {
    val joinQuery = (for {
      (news, newsRecommends) <- newsList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow))
        .join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).filter(_.level === 5.0).filter(_.bigimg.getOrElse(0) > 0).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid))).on(_.nid === _.nid).sortBy(_._2.rtime.desc).drop(offset).take(limit)
    } yield (news, newsRecommends)).map {
      case (n, newsRecommends) =>
        (n, newsRecommends)
    }

    db.run(joinQuery.result)
  }

  //必须出,等级为5非大图
  def listNewsByRecommandUid5(uid: Long, offset: Long, limit: Long): Future[Seq[(NewsRow, NewsRecommend)]] = {
    val joinQuery = (for {
      (news, newsRecommends) <- newsList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow))
        .join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).filter(_.level === 5.0).filter(_.bigimg.getOrElse(0) < 1).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid))).on(_.nid === _.nid).sortBy(_._2.rtime.desc).drop(offset).take(limit)
    } yield (news, newsRecommends)).map {
      case (n, newsRecommends) =>
        (n, newsRecommends)
    }

    db.run(joinQuery.result)
  }

  //模型从人工推荐新闻中选大图
  def refreshByPeopleRecommendBigImg(uid: Long, offset: Long, limit: Long): Future[Seq[(NewsRow, NewsRecommend)]] = {
    val joinQuery = (for {
      (news, newsRecommends) <- newsList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.nid in newsRecommendForUserList.filter(_.uid === uid).filter(_.sourcetype === 1).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid))
        .join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid))).on(_.nid === _.nid).sortBy(_._2.level.desc).drop(offset).take(limit)
    } yield (news, newsRecommends)).map {
      case (n, newsRecommends) =>
        (n, newsRecommends)
    }
    db.run(joinQuery.result)
  }

  //冷启动时,人工大图
  def listNewsByRecommandUidBigImg(uid: Long, offset: Long, limit: Long): Future[Seq[(NewsRow, NewsRecommend)]] = {
    val joinQuery = (for {
      (news, newsRecommends) <- newsList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow))
        .join(newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).filter(_.bigimg > 0).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid))).on(_.nid === _.nid).sortBy(p => (p._2.level.desc, p._2.rtime.desc)).drop(offset).take(limit)
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

  def load(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.imgs.nonEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.ctime < timeCursor).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  //新闻刷到头了,用6小时以内没看过的新闻补上
  def refresh(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.imgs.nonEmpty).filter(_.ctime > LocalDateTime.now().plusHours(newsWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid)).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def loadByHot(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.ctime < timeCursor).filter(_.comment > 0).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.comment.desc).drop(offset).take(limit).result)
  }

  def loadByHotWord(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.nid in newsRecommendHotList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.ctime.desc).take(limit).map(_.nid)).sortBy(_.ctime.asc).drop(offset).take(limit).result)
  }

  def loadByModelRecommend(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.nid in newsRecommendForUserList.filter(_.uid === uid).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.predict.desc).take(limit).map(_.nid)).result)
  }

  def refreshByHot(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.ctime > timeCursor).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.comment > 0).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.comment.desc).drop(offset).take(limit).result)
  }

  def refreshByHotWord(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.nid in newsRecommendHotList.filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.ctime.desc).take(limit).map(_.nid)).sortBy(_.ctime.desc).drop(offset).take(limit).result)
  }

  def refreshByModelRecommend(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.nid in newsRecommendForUserList.filter(_.uid === uid).filter(_.sourcetype === 1).filter(_.ctime > LocalDateTime.now().plusDays(recommendtimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.predict.desc).take(limit).map(_.nid)).result)
  }

  //  def refreshByPeopleRecommend(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
  //    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.nid in newsRecommendForUserList.filter(_.uid === uid).filter(_.sourcetype === 0).filter(_.ctime > LocalDateTime.now().plusDays(recommendtimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.predict.desc).take(limit).map(_.nid)).result)
  //  }

  def refreshByPeopleRecommend(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.nid in newsRecommendList.filter(_.rtime > LocalDateTime.now().plusHours(newsrecommendtimeWindow)).map(_.nid)).filter(_.chid in newsList.filter(_.nid in newsClickList.filter(_.uid === uid).filter(_.ctime > LocalDateTime.now().plusDays(recommendtimeWindow)).map(_.nid)).groupBy(_.chid).map { case (chid, css) => (chid, css.length) }.sortBy(_._2.desc).map(_._1).take(3)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.ctime.desc).take(limit).result)
  }

  def refreshByClick(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.imgs.nonEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.chid in newsList.filter(_.nid in newsClickList.filter(_.uid === uid).filter(_.ctime > LocalDateTime.now().plusDays(recommendtimeWindow)).map(_.nid)).groupBy(_.chid).map { case (chid, css) => (chid, css.length) }.sortBy(_._2.desc).map(_._1).take(3)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).sortBy(_.ctime.desc).take(limit).result)
  }

  def refreshByLike(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsRow]] = {
    db.run(newsList.filter(_.chid =!= shieldedCid).filter(_.state === 0).filterNot(_.pname inSet filterSet).filter(_.sechid.isEmpty).filter(_.imgs.nonEmpty).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filter(_.nid in newsRecommendLikeList.filter(_.uid === uid).filter(_.ctime > LocalDateTime.now().plusDays(newstimeWindow)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > LocalDateTime.now().plusDays(newstimeWindow)).map(_.nid)).map(_.nid)).sortBy(_.ctime.desc).take(limit).result)
  }

}