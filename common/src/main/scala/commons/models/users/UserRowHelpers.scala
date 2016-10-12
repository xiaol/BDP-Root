package commons.models.users

import java.util.UUID

import commons.models.users.UserType._
import commons.models.users.UserRole._
import commons.utils.PasswordUtils._
import commons.utils.Base64Utils
import org.joda.time.LocalDateTime

/**
 * Created by zhange on 2016-04-21.
 *
 */

object UserRowHelpers extends UserRowHelpers

trait UserRowHelpers {

  def from(user: User): UserRow = user match {
    case u: UserGuest => {
      val sys: UserRowSys = UserRowSys(ctime = LocalDateTime.now().withMillisOfSecond(0), ltime = LocalDateTime.now().withMillisOfSecond(0), platf = u.platform, urole = GUEST_ROLE_CODE, utype = u.utype)
      val base: UserRowBase = UserRowBase(password = Some(Base64Utils.encodeBase64(UUID.randomUUID().toString)))
      val profile: UserRowProfile = UserRowProfile(province = u.province, city = u.city, district = u.district)
      val social: UserRowSocial = UserRowSocial()
      UserRow(sys, base, profile, social)
    }
    case u: UserSocial => {
      val sys: UserRowSys = UserRowSys(ctime = LocalDateTime.now().withMillisOfSecond(0), ltime = LocalDateTime.now().withMillisOfSecond(0), platf = u.platform, urole = REGIST_ROLE_CODE, utype = u.utype)
      val base: UserRowBase = UserRowBase(uname = u.uname, gender = u.gender, avatar = u.avatar)
      val profile: UserRowProfile = UserRowProfile(averse = u.averse, prefer = u.prefer, province = u.province, city = u.city, district = u.district)
      val social: UserRowSocial = UserRowSocial(suid = Some(u.suid), stoken = Some(u.stoken), sexpires = Some(u.sexpires))
      UserRow(sys, base, profile, social)
    }
    case u: UserLocal => {
      val sys: UserRowSys = UserRowSys(ctime = LocalDateTime.now().withMillisOfSecond(0), ltime = LocalDateTime.now().withMillisOfSecond(0), platf = u.platform, urole = REGIST_ROLE_CODE, utype = u.utype)
      val passwordSalt: String = createPasswordSalt()
      val encryptedPassword: String = hashAndStretch(u.password, passwordSalt, STRETCH_LOOP_COUNT)
      val base: UserRowBase = UserRowBase(email = Some(u.email), verified = Some(false), password = Some(encryptedPassword), passsalt = Some(passwordSalt), uname = u.uname, gender = u.gender, avatar = u.avatar)
      val profile: UserRowProfile = UserRowProfile(averse = u.averse, prefer = u.prefer, province = u.province, city = u.city, district = u.district)
      val social: UserRowSocial = UserRowSocial()
      UserRow(sys, base, profile, social)
    }
  }

  def toResponse(userRow: UserRow): UserResponse = {
    val password: Option[String] = userRow.sys.utype match {
      case GUEST_TYPE_CODE  => userRow.base.password
      case WEIBO_TYPE_CODE  => userRow.base.password
      case WEIXIN_TYPE_CODE => userRow.base.password
      case _                => None
    }
    UserResponse(userRow.sys.utype, userRow.sys.uid.get, password, userRow.base.uname, userRow.base.avatar, userRow.profile.channel)
  }

  def merge(origin: UserRow, update: UserRow): UserRow = {
    val sys: UserRowSys = merge(origin.sys, update.sys)
    val base: UserRowBase = merge(origin.base, update.base)
    val profile: UserRowProfile = merge(origin.profile, update.profile)
    val social: UserRowSocial = merge(origin.social, update.social)
    UserRow(sys, base, profile, social)
  }

  def merge(origin: UserRow, profile: UserRowProfile): UserRow = {
    origin.copy(profile = merge(origin.profile, profile))
  }

  /**
   * Update UserRowSys without fields: uid, create
   */
  def merge(origin: UserRowSys, update: UserRowSys): UserRowSys = {
    origin.copy(ltime = update.ltime, platf = update.platf, urole = update.urole, utype = update.utype)
  }

  /**
   * Update UserRowBase without fields: email, verified, password, passsalt
   */
  def merge(origin: UserRowBase, update: UserRowBase): UserRowBase = {
    val uname: Option[String] = if (update.uname.isDefined) update.uname else origin.uname
    val email: Option[String] = if (update.email.isDefined) update.email else origin.email
    val password: Option[String] = if (update.password.isDefined) update.password else origin.password
    val passsalt: Option[String] = if (update.passsalt.isDefined) update.passsalt else origin.passsalt
    val gender: Option[Int] = if (update.gender.isDefined) update.gender else origin.gender
    val avatar: Option[String] = if (update.avatar.isDefined) update.avatar else origin.avatar
    origin.copy(uname = uname, email = email, password = password, passsalt = passsalt, gender = gender, avatar = avatar)
  }

  /**
   * Update UserRowProfile with merge each field.
   */
  def merge(origin: UserRowProfile, update: UserRowProfile): UserRowProfile = {
    val channel: Option[List[String]] = mergeOptSeqStr(origin.channel, update.channel)
    val averse: Option[List[String]] = mergeOptSeqStr(origin.averse, update.averse)
    val prefer: Option[List[String]] = mergeOptSeqStr(origin.prefer, update.prefer)
    val province: Option[String] = if (update.province.isDefined) update.province else origin.province
    val city: Option[String] = if (update.city.isDefined) update.city else origin.city
    val district: Option[String] = if (update.district.isDefined) update.district else origin.district
    UserRowProfile(channel, averse, prefer, province, city, district)
  }

  /**
   * Update UserRowSocial with replace old fields with new fields, if exists.
   */
  def merge(origin: UserRowSocial, update: UserRowSocial): UserRowSocial = {
    val suid: Option[String] = if (update.suid.isDefined) update.suid else origin.suid
    val stoken: Option[String] = if (update.stoken.isDefined) update.stoken else origin.stoken
    val sexpires: Option[LocalDateTime] = if (update.sexpires.isDefined) update.sexpires else origin.sexpires
    UserRowSocial(suid, stoken, sexpires)
  }

  private val mergeOptSeqStr: ((Option[List[String]], Option[List[String]]) => Option[List[String]]) = (origin: Option[List[String]], update: Option[List[String]]) => {
    if (update.isDefined) Some(update.get ++ origin.getOrElse(List[String]())) else origin
  }
}
