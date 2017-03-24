package dao.news

import javax.inject.{ Inject, Singleton }

import commons.models.news.NewsFeedRow
import org.joda.time.LocalDateTime
import play.api.db.slick._
import play.db.NamedDatabase
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

  final private val select: String = "select nv.nid, nv.docid, nv.title, nv.pname, nv.purl, nv.chid, nv.collect, nv.concern, nv.un_concern, nv.comment, nv.style, array_to_string(nv.imgs, ',') as imgs,  nv.icon, nv.videourl, nv.duration, nv.thumbnail, nv.rtype, 0 as logtype from newslist_v2 nv"
  final private val condition: String = " and nv.chid != 28 and nv.state=0 and nv.pname not in('就是逗你笑', 'bomb01') and nv.sechid is null "

}

@Singleton
class NewsResponseDao @Inject() (@NamedDatabase("pg2") protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[MyPostgresDriver] {
  import NewsResponseDao._
  import driver.api._

  def byChannel(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long, chid: Long): Future[Seq[NewsFeedRow]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3) and nv.chid=$chid and nv.ctime>#$dayWindow3  #$condition offset $offset limit $limit ".as[NewsFeedRow]
    db.run(action)
  }

  def bySeChannel(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long, chid: Long, sechid: Long): Future[Seq[NewsFeedRow]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3) and nv.chid=$chid and nv.sechid=$sechid and nv.ctime>#$dayWindow3  #$condition offset $offset limit $limit ".as[NewsFeedRow]
    db.run(action)
  }

  def byChannelWithKmeans(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long, chid: Long): Future[Seq[NewsFeedRow]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where nv.chid=$chid and nv.ctime>#$dayWindow3 and nv.nid in (select nid from news_kmeans nk inner join user_kmeans_cluster ukc on nk.model_v=ukc.model_v and nk.cluster_id=ukc.cluster_id and nk.chid=ukc.chid where not exists (select 1 from #$tablename nr where nk.nid=nr.nid and nr.uid=$uid and nr.readtime>#$dayWindow3) and ukc.uid=$uid and nk.ctime>#$dayWindow3 and ukc.chid=$chid order by random() limit $limit) ".as[NewsFeedRow]
    db.run(action)
  }

  def video(offset: Long, limit: Long, timeCursor: LocalDateTime, uid: Long): Future[Seq[NewsFeedRow]] = {
    val tablename: String = "newsrecommendread_" + uid % 100
    val action = sql" #$select where  not exists (select 1 from #$tablename nr where nv.nid=nr.nid and nr.uid=$uid and nr.readtime>#$hourWindow24) and nv.ctime>#$hourWindow24  and nv.rtype=6 limit $limit ".as[NewsFeedRow]
    db.run(action)
  }

}
