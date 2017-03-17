package dao.news

import java.sql.Timestamp
import javax.inject.{ Inject, Singleton }

import org.joda.time.LocalDateTime
import play.api.db.slick._
import play.db.NamedDatabase
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }
/**
 * Created by zhangshl on 17/2/22.
 */
object NewsUnionFeedDao {
  final private val dayWindow1: String = " now()-interval'1 day' "
  final private val dayWindow2: String = " now()-interval'2 day' "
  final private val dayWindow3: String = " now()-interval'3 day' "
  final private val dayWindow7: String = " now()-interval'7 day' "
  final private val bigimagedayWindow: String = " now()-interval'3 day' " //大图过年期间10天,平时3天
  final private val timeWindow = (timeCursor: LocalDateTime) => timeCursor.plusDays(-1)

  final private val channelFilterSet = Set(2L, 4L, 6L, 7L, 9L) //模型推荐这几个频道, 频道推荐就不推这些频道

  final private val select: String = "select nv.nid, nv.url, nv.docid, nv.title, nv.pname, nv.purl, nv.collect, nv.concern, nv.comment, nv.inum, nv.style, array_to_string(nv.imgs, ',') as imgs, nv.state, nv.ctime, nv.chid, nv.icon, nv.videourl, nv.thumbnail, nv.duration "
  final private val condition: String = " and nv.chid != 28 and nv.state=0 and nv.pname not in('就是逗你笑', 'bomb01') and nv.sechid is null "

}

@Singleton
class NewsUnionFeedDao @Inject() (@NamedDatabase("pg2") protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._
  import NewsUnionFeedDao._

  //前一天点击量前100的新闻 和 没出现过的1天内的新闻
  def common(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val limitclick = limit / 2
    val action = sql" select * from (#$select , 0 as rtype, 100 as logtype from newslist_v2 nv inner join newsclickorder nc on nv.nid=nc.nid where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow2)  and nc.ctype=0 and nc.ctime>#$dayWindow1 and nv.ctime>#$dayWindow2 limit $limitclick)click union all select * from (#$select, 0 as rtype, 0 as logtype from newslist_v2 nv where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow1) and nv.ctime>#$dayWindow1  #$condition and nv.imgs is not null limit $limit)common ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]
    db.run(action)
  }

  def hot(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val limitbaidu = limit / 2
    val action = sql"select * from (#$select , 1 as rtype, 11 as logtype from newslist_v2 nv where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow2) and nv.nid in (select nid from newsrecommendhot where ctime>#$dayWindow2) and nv.ctime>#$dayWindow2 #$condition limit $limitbaidu )baiduhot union all select * from (#$select , 1 as rtype, 12 as logtype from newslist_v2 nv where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow1) and nv.ctime>#$dayWindow1 #$condition and nv.imgs is not null and nv.comment>0 limit $limit)hot ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]
    db.run(action)
  }

  def byLDAandKmeans(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]] = {
    val tablename1: String = "newsrecommendread_" + uid % 100
    val tablename2: String = "newsrecommendforuser_" + uid % 10
    val action = sql"select * from ( #$select , 21 as rtype, 21 as logtype from newslist_v2 nv where  not exists (select 1 from #$tablename1 nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3) and nv.nid in (select nid from #$tablename2 where uid=$uid and ctime>#$dayWindow3 and sourcetype=1) and nv.ctime>#$dayWindow3 #$condition limit $limit )lda union all select * from (#$select , 21 as rtype, 22 as logtype from newslist_v2 nv where  not exists (select 1 from #$tablename1 nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3) and nv.nid in (select nid from #$tablename2 where uid=$uid and ctime>#$dayWindow3 and sourcetype=2) and nv.ctime>#$dayWindow3 #$condition limit $limit)kmeans ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]
    db.run(action)
  }

  //在人工1天内推荐里,用户偏好前3的频道,3天内的新闻
  def byPeopleRecommendWithClick(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" select * from (#$select , 2 as rtype, 23 as logtype from newslist_v2 nv where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow1) and nv.chid in (select chid from newslist_v2 where nid in (select nid from newsrecommendclick where uid=$uid and ctime>now()-interval'10 day') group by chid order by count(1) desc limit 3) and nv.chid not in (2, 4, 6, 7, 9) and nv.nid in (select nid from newsrecommendlist where rtime>#$dayWindow1) and nv.ctime>#$dayWindow3 #$condition limit $limit)r1 union all select * from (#$select , 2 as rtype, 24 as logtype from newslist_v2 nv inner join newsrecommendlist nl on nv.nid=nl.nid where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow1) and nl.rtime>#$dayWindow1 and nv.ctime>#$dayWindow3 #$condition order by nl.level desc,nl.rtime desc limit $limit)r2 ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]
    db.run(action)
  }

  //在人工3天内推荐里,大图新闻, 和视频
  def byBigImageAndVideo(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" select * from (select nv.nid, url, docid, title, pname, purl, collect, concern, comment, inum, (10+nr.bigimg) as style, array_to_string(nv.imgs, ',') as imgs, state, ctime, chid, icon, videourl, thumbnail, duration, 999 as rtype, 25 as logtype from newslist_v2 nv inner join newsrecommendlist nr on nv.nid=nr.nid where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3)  and nv.ctime>#$dayWindow3 and nr.rtime>#$dayWindow3 and nr.bigimg >0 order by level desc, rtime desc limit $limit)bigimage union all select * from(#$select , 6 as rtype, 6 as logtype from newslist_v2 nv where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow1) and nv.ctime>#$dayWindow1  and nv.rtype=6 limit $limit)video ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]
    db.run(action)
  }

  //在人工3天内推荐里,大图新闻
  def byBigImage(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" select nv.nid, url, docid, title, pname, purl, collect, concern, comment, inum, (10+nr.bigimg) as style, array_to_string(nv.imgs, ',') as imgs, state, ctime, chid, icon, videourl, thumbnail, duration, 999 as rtype, 25 as logtype from newslist_v2 nv inner join newsrecommendlist nr on nv.nid=nr.nid where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3)  and nv.ctime>#$dayWindow3 and nr.rtime>#$dayWindow3 and nr.bigimg >0 order by level desc, rtime desc limit $limit ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]
    db.run(action)
  }

  def commonAll(limit: Long): Future[Seq[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]] = {
    val limitclick = limit / 2
    val action = sql" select * from (#$select , 0 as rtype, 100 as logtype from newslist_v2 nv inner join newsclickorder nc on nv.nid=nc.nid where nc.ctype=0 and nc.ctime>#$dayWindow2 and nv.ctime>#$dayWindow2 limit $limitclick)click union all select * from (#$select, 0 as rtype, 0 as logtype from newslist_v2 nv where nv.ctime>#$dayWindow1  #$condition and nv.imgs is not null limit $limit)common ".as[(Long, String, String, String, Option[String], Option[String], Int, Int, Int, Int, Int, Option[String], Int, Timestamp, Long, Option[String], Option[String], Option[String], Option[Int], Option[Int], Option[Int])]
    db.run(action)
  }

}