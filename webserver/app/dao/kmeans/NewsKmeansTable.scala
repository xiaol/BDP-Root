package dao.kmeans

/**
 * Created by zhangshl on 17/1/11.
 */

import javax.inject.{ Inject, Singleton }

import commons.models.kmeans.NewsKmeans
import commons.models.news.NewsRow
import dao.news.{ NewsRecommendReadTable, NewsTable }
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ Future, ExecutionContext }

trait NewsKmeansTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsKmeansTable(tag: Tag) extends Table[NewsKmeans](tag, "news_kmeans") {

    def nid = column[Long]("nid")
    def model_v = column[String]("model_v")
    def ch_name = column[String]("ch_name")
    def cluster_id = column[Long]("cluster_id")
    def chid = column[Long]("chid")

    def * = (nid, model_v, ch_name, cluster_id, chid) <> ((NewsKmeans.apply _).tupled, NewsKmeans.unapply)
  }
}

object NewsKmeansDAO {
  final private val timeWindow = LocalDateTime.now().plusDays(-3)
}

@Singleton
class NewsKmeansDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends NewsKmeansTable
    with UserKmeansClusterTable with NewsTable with NewsRecommendReadTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._
  import NewsKmeansDAO._

  val newsKmeansList = TableQuery[NewsKmeansTable]
  val userKmeansClusterList = TableQuery[UserKmeansClusterTable]
  val newsRecommendReadList = TableQuery[NewsRecommendReadTable]
  val newsList = TableQuery[NewsTable]

  def queryByChannelWithKmeans(uid: Long, chid: Long, limit: Long): Future[Seq[NewsRow]] = {
    val rand = SimpleFunction.nullary[Double]("random")
    val joinQuery = (for {
      (((userKmeans, newsKmeans), news)) <- userKmeansClusterList.filter(_.uid === uid).filter(_.chid === chid).sortBy(_.times.desc).take(3)
        .join(newsKmeansList.filter(_.chid === chid).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > timeWindow).map(_.nid))).on(_.cluster_id === _.cluster_id)
        .join(newsList.filter(_.chid === chid).filter(_.state === 0).filter(_.ctime > timeWindow)).on(_._2.nid === _.nid).sortBy(x => rand).take(limit)
    } yield (news))

    db.run(joinQuery.result)
  }

  //  def refreshByChannel(uid: Long, chid: Long, limit: Long): Future[Seq[NewsRow]] = {
  //    db.run(newsList.filter(_.chid === chid).filter(_.state === 0).filter(_.ctime > timeWindow).filter(_.nid in newsKmeansList.filter(_.chid === chid).filter(_.cluster_id in userKmeansClusterList.filter(_.chid === chid).filter(_.uid === uid).sortBy(_.times.desc).take(3).map(_.cluster_id)).filterNot(_.nid in newsRecommendReadList.filter(_.uid === uid).filter(_.readtime > timeWindow).map(_.nid)).map(_.nid)).sortBy(_.ctime.desc).take(limit).result)
  //  }

}