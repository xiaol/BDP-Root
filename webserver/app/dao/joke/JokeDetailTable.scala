package dao.joke

import javax.inject.{ Inject, Singleton }

import commons.models.joke.{ JokeDetailRow, JokeDetailRowSyst, JokeDetailRowBase }
import org.joda.time._
import play.api.db.slick._
import play.api.libs.json.JsValue
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-10.
 *
 */

trait JokeDetailTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class JokeDetailTable(tag: Tag) extends Table[JokeDetailRow](tag, "info_joke") {

    def nid = column[Long]("nid")
    def docid = column[String]("docid")
    def content = column[JsValue]("content")
    def author = column[Option[String]]("author")
    def avatar = column[Option[String]]("avatar")
    def ptime = column[LocalDateTime]("ptime")
    def pname = column[Option[String]]("pname")

    def style = column[Option[Int]]("style")
    def imgs = column[Option[List[String]]]("imgs")
    def ctime = column[LocalDateTime]("ctime")
    def chid = column[Long]("chid")
    def sechid = column[Option[Long]]("sechid")
    def srid = column[Option[Long]]("srid")
    def icon = column[Option[String]]("icon")

    def base = (nid, docid, content, author, avatar, ptime, pname) <> ((JokeDetailRowBase.apply _).tupled, JokeDetailRowBase.unapply)
    def syst = (style, imgs, ctime, chid, sechid, srid, icon) <> ((JokeDetailRowSyst.apply _).tupled, JokeDetailRowSyst.unapply)
    def * = (base, syst) <> ((JokeDetailRow.apply _).tupled, JokeDetailRow.unapply)
  }
}

@Singleton
class JokeDetailDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends JokeDetailTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val jokeDetailList = TableQuery[JokeDetailTable]

  def findDetailByNid(nid: Long): Future[Option[JokeDetailRow]] = {
    db.run(jokeDetailList.filter(_.nid === nid).result.map(_.headOption))
  }

  def findByDocid(docid: String): Future[Option[JokeDetailRow]] = {
    db.run(jokeDetailList.filter(_.docid === docid).result.map(_.headOption))
  }

}