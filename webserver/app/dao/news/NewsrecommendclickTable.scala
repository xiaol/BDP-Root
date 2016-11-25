package dao.news

import java.sql.Date
import javax.inject.{ Inject, Singleton }

import commons.models.news.Newsrecommendclick
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-09.
 *
 */

trait NewsrecommendclickTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class NewsrecommendclickTable(tag: Tag) extends Table[Newsrecommendclick](tag, "newsrecommendclick") {
    def uid = column[Long]("uid")
    def nid = column[Long]("nid")
    def ctime = column[Date]("ctime")

    def * = (uid, nid, ctime) <> ((Newsrecommendclick.apply _).tupled, Newsrecommendclick.unapply)
  }
}

@Singleton
class NewsrecommendclickDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends NewsrecommendclickTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  lazy val people = TableQuery[NewsrecommendclickTable]

  def selectNewsrecommendclicks(nids: Seq[Long]): Future[Seq[(Long, Int, Int)]] = {
    val action = sql"select t.nid,COALESCE(t1.c1,0), COALESCE(t2.c2,0) as c2 from newslist_v2 t left join (select nid ,count(1) as c1  from newsrecommendread  where nid in (#${nids.mkString(",")}) group by nid)t1 on t.nid=t1.nid left join (select nid ,count(1) as c2 from (select distinct uid,nid from newsrecommendclick where nid in (#${nids.mkString(",")}))t group by nid)t2 on t1.nid=t2.nid where t.nid in (#${nids.mkString(",")}) ".as[(Long, Int, Int)]
    db.run(action)
  }

}
