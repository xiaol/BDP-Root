package dao.users

import javax.inject.{ Inject, Singleton }

import commons.models.users._
import org.joda.time._
import play.api.db.slick._
import utils.MyPostgresDriver

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-04-19.
 *
 */

trait UserTable { self: HasDatabaseConfig[MyPostgresDriver] =>
  import driver.api._

  class UserTable(tag: Tag) extends Table[UserRow](tag, "userlist_v2") {
    def uid = column[Long]("uid", O.PrimaryKey, O.AutoInc)
    def ctime = column[LocalDateTime]("ctime")
    def ltime = column[LocalDateTime]("ltime")
    def platf = column[Int]("platf")
    def urole = column[Int]("urole")
    def utype = column[Int]("utype")

    def email = column[Option[String]]("email")
    def verified = column[Option[Boolean]]("verified")
    def password = column[Option[String]]("password")
    def passsalt = column[Option[String]]("passsalt")
    def uname = column[Option[String]]("uname")
    def gender = column[Option[Int]]("gender")
    def avatar = column[Option[String]]("avatar")

    def channel = column[Option[List[String]]]("channel")
    def averse = column[Option[List[String]]]("averse")
    def prefer = column[Option[List[String]]]("prefer")
    def province = column[Option[String]]("province")
    def city = column[Option[String]]("city")
    def district = column[Option[String]]("district")

    def suid = column[Option[String]]("suid")
    def stoken = column[Option[String]]("stoken")
    def sexpires = column[Option[LocalDateTime]]("sexpires")

    def sysFields = (uid.?, ctime, ltime, platf, urole, utype) <> ((UserRowSys.apply _).tupled, UserRowSys.unapply)
    def baseFields = (email, verified, password, passsalt, uname, gender, avatar) <> ((UserRowBase.apply _).tupled, UserRowBase.unapply)
    def profileFields = (channel, averse, prefer, province, city, district) <> ((UserRowProfile.apply _).tupled, UserRowProfile.unapply)
    def socialFields = (suid, stoken, sexpires) <> ((UserRowSocial.apply _).tupled, UserRowSocial.unapply)

    def * = (sysFields, baseFields, profileFields, socialFields) <> ((UserRow.apply _).tupled, UserRow.unapply)
  }
}

@Singleton
class UserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserTable with HasDatabaseConfigProvider[MyPostgresDriver] {
  import driver.api._

  val userList = TableQuery[UserTable]

  def findByUid(uid: Long): Future[Option[UserRow]] = {
    db.run(userList.filter(_.uid === uid).result.map(_.headOption))
  }

  def findByEmail(email: String): Future[Option[UserRow]] = {
    db.run(userList.filter(_.email === email).result.map(_.headOption))
  }

  def findByUname(uname: String): Future[Option[UserRow]] = {
    db.run(userList.filter(_.uname === uname).result.map(_.headOption))
  }

  def findBySuid(suid: String): Future[Option[UserRow]] = {
    db.run(userList.filter(_.suid === suid).result.map(_.headOption))
  }

  def list(offset: Long, limit: Long): Future[Seq[UserRow]] = {
    db.run(userList.drop(offset).take(limit).result)
  }

  def count(): Future[Int] = {
    db.run(userList.length.result)
  }

  def insert(userRow: UserRow): Future[Long] = {
    db.run(userList returning userList.map(_.uid) += userRow)
  }

  def getChannel(uid: Long): Future[Option[List[String]]] = {
    db.run(userList.filter(_.uid === uid).map(_.channel).result.map(_.headOption)).map { chOpt =>
      chOpt.flatMap {
        case ch @ Some(_) => ch
        case _            => None
      }
    }
  }

  def update(uid: Long, userRow: UserRow): Future[Option[UserRow]] = {
    db.run(userList.filter(_.uid === uid).update(userRow).map {
      case 0 => None
      case _ => Some(userRow)
    })
  }

  def updateLoginTime(uid: Long, ltime: LocalDateTime): Future[Option[Long]] = {
    db.run(userList.filter(_.uid === uid).map(_.ltime).update(ltime).map {
      case 0 => None
      case _ => Some(uid)
    })
  }

  def updateChannel(uid: Long, channels: List[String]): Future[Option[List[String]]] = {
    db.run(userList.filter(_.uid === uid).map(_.channel).update(Some(channels)).map {
      case 0 => None
      case _ => Some(channels)
    })
  }

  def delete(uid: Long): Future[Option[Long]] = {
    db.run(userList.filter(_.uid === uid).delete.map {
      case 0 => None
      case _ => Some(uid)
    })
  }
}
