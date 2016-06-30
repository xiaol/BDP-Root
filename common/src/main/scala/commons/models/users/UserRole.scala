package commons.models.users

/**
 * Created by zhange on 2016-04-21.
 *
 */

sealed trait UserRole

case object AdminiRole extends UserRole
case object RegistRole extends UserRole
case object GuestRole extends UserRole

object UserRole {
  final val ADMIN_ROLE_CODE: Int = 0
  final val REGIST_ROLE_CODE: Int = 1
  final val GUEST_ROLE_CODE: Int = 2
}