package dao.pvuvcount

import javax.inject.{ Inject, Singleton }

import commons.models.advertisement.Slide
import org.joda.time.LocalDateTime
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ Future, ExecutionContext }

trait SlideCountTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class SlideCountTable(tag: Tag) extends Table[Slide](tag, "slide_count") {

    def mid = column[String]("mid")
    def uid = column[Long]("uid")
    def ctype = column[Int]("ctype")
    def ptype = column[Int]("ptype")
    def ctime = column[Option[LocalDateTime]]("ctime")
    def ipaddress = column[Option[String]]("ipaddress")
    def version_text = column[Option[String]]("version_text")
    def operate_type = column[Option[Int]]("operate_type")

    def * = (mid, uid, ctype, ptype, ctime, ipaddress, version_text, operate_type) <> ((Slide.apply _).tupled, Slide.unapply)
  }
}

@Singleton
class SlideCountDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends SlideCountTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val slideCountList = TableQuery[SlideCountTable]

  def insert(slide: Slide): Future[Long] = {
    db.run(slideCountList returning slideCountList.map(_.uid) += slide)
  }

}