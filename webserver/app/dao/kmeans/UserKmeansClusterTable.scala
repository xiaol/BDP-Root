package dao.kmeans

/**
 * Created by zhangshl on 17/1/11.
 */

import javax.inject.{ Inject, Singleton }

import commons.models.kmeans.UserKmeansCluster
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.ExecutionContext

trait UserKmeansClusterTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class UserKmeansClusterTable(tag: Tag) extends Table[UserKmeansCluster](tag, "user_kmeans_cluster") {

    def uid = column[Long]("uid")
    def model_v = column[String]("model_v")
    def ch_name = column[String]("ch_name")
    def cluster_id = column[Long]("cluster_id")
    def chid = column[Long]("chid")
    def times = column[Int]("times")
    def create_time = column[Option[LocalDateTime]]("create_time")
    def fail_time = column[Option[LocalDateTime]]("fail_time")

    def * = (uid, model_v, ch_name, cluster_id, chid, times, create_time, fail_time) <> ((UserKmeansCluster.apply _).tupled, UserKmeansCluster.unapply)
  }
}

@Singleton
class UserKmeansClusterDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserKmeansClusterTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val userKmeansClusterList = TableQuery[UserKmeansClusterTable]

}