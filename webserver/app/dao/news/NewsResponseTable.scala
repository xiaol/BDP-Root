package dao.news

import java.sql.Timestamp
import javax.inject.{ Inject, Singleton }

import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-09.
 *
 */

object NewsResponseDao {
  final private val dayWindow3: String = " now()-interval'3 day' "
  final private val dayWindow7: String = " now()-interval'7 day' "
  final private val hourWindow24: String = " now()-interval'24 hour' " //24小时
  final private val bigimagedayWindow: String = " now()-interval'3 day' " //大图过年期间10天,平时3天
  final private val timeWindow = (timeCursor: LocalDateTime) => timeCursor.plusDays(-1)

  final private val channelFilterSet = Set(2L, 4L, 6L, 7L, 9L) //模型推荐这几个频道, 频道推荐就不推这些频道

  final private val select: String = "select nv.nid, nv.url, nv.docid, nv.title, nv.pname, nv.purl, nv.collect, nv.concern, nv.comment, nv.inum, nv.style, nv.imgs, nv.state, nv.ctime, nv.chid, nv.icon, nv.videourl, nv.thumbnail, nv.duration, nv.rtype from newslist_v2 nv"
  final private val condition: String = " and nv.chid != 28 and nv.state=0 and nv.pname not in('就是逗你笑', 'bomb01') and nv.sechid is null "

}

@Singleton
class NewsResponseDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._
  import NewsResponseDao._

  def news(uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$hourWindow24) and nv.ctime>#$hourWindow24  #$condition offset 0 limit 1 ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  //没出现过的1天内的新闻
  def common(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$hourWindow24) and nv.ctime>#$hourWindow24  #$condition and nv.imgs is not null offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  //没出现过,有评论的1天内的新闻
  def hot(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$hourWindow24) and nv.ctime>#$hourWindow24 #$condition and nv.imgs is not null and nv.comment>0 order by nv.comment desc limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  def baiduHotWord(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3) and nv.nid in (select nid from newsrecommendhot where ctime>#$dayWindow3) and nv.ctime>#$dayWindow3 #$condition offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  def byLDA(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename1: String = "newsrecommendread_" + uid % 100
    val tablename2: String = "newsrecommendforuser_" + uid % 10
    val action = sql" #$select where  not exists (select 1 from #$tablename1 nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow7) and nv.nid in (select nid from #$tablename2 where uid=$uid and ctime>#$dayWindow7 and sourcetype=1) and nv.ctime>#$dayWindow7 #$condition offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  def byKmeans(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename1: String = "newsrecommendread_" + uid % 100
    val tablename2: String = "newsrecommendforuser_" + uid % 10
    val action = sql" #$select where  not exists (select 1 from #$tablename1 nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow7) and nv.nid in (select nid from #$tablename2 where uid=$uid and ctime>#$dayWindow7 and sourcetype=2) and nv.ctime>#$dayWindow7 #$condition offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  //在人工1天内推荐里
  def byPeopleRecommend(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select inner join newsrecommendlist nl on nv.nid=nl.nid where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$hourWindow24) and nl.rtime>#$hourWindow24 and nv.ctime>#$dayWindow3 #$condition order by nl.level desc,nl.rtime desc offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  //在人工1天内推荐里,用户偏好前3的频道,3天内的新闻
  def byPeopleRecommendWithClick(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$hourWindow24) and nv.chid in (select chid from newslist_v2 where nid in (select nid from newsrecommendclick where uid=$uid and ctime>now()-interval'30 day') group by chid order by count(1) desc limit 3) and nv.chid not in (2, 4, 6, 7, 9) and nv.nid in (select nid from newsrecommendlist where rtime>#$hourWindow24) and nv.ctime>#$dayWindow3 #$condition offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  //在人工3天内推荐里,大图新闻
  def byBigImage(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" select nv.nid, url, docid, title, pname, purl, collect, concern, comment, inum, (10+nr.bigimg) as style, imgs, state, ctime, chid, icon, videourl, thumbnail, duration, rtype from newslist_v2 nv inner join newsrecommendlist nr on nv.nid=nr.nid where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3)  and nv.ctime>#$dayWindow3 and nr.rtime>#$dayWindow3 and nr.bigimg >0 order by level desc, rtime desc  offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  def byChannel(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long, chid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow7) and nv.chid=$chid and nv.ctime>#$dayWindow7  #$condition and rtype is null offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  def bySeChannel(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long, chid: Long, sechid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow7) and nv.chid=$chid and nv.sechid=$sechid and nv.ctime>#$dayWindow7  #$condition and rtype is null offset $offset limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  def byChannelWithKmeans(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long, chid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where nv.chid=$chid and nv.ctime>#$dayWindow3 and nv.nid in (select nid from news_kmeans nk inner join user_kmeans_cluster ukc on nk.model_v=ukc.model_v and nk.cluster_id=ukc.cluster_id and nk.chid=ukc.chid where not exists (select 1 from #$tablename nr where nk.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3) and ukc.uid=$uid and nk.ctime>#$dayWindow3 and ukc.chid=$chid order by random() limit $limit) ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  def video(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$hourWindow24) and nv.ctime>#$hourWindow24  and nv.rtype=6 limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int])]
    db.run(action)
  }

  //  def news(): Future[Seq[(Long, String, Option[String])]] = {
  //    val tablename = "newslist_v" + 2 % 10
  //    val action = sql"select nid, title, imgs from #$tablename where imgs is not null limit 10 ".as[(Long, String, Option[String])]
  //    db.run(action)
  //  }

}
