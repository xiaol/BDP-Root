package controllers

import javax.inject.Inject

import commons.models.users._
import commons.models.users.UserGuest._
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.libs.json._
import play.api.mvc._
import utils.Response._
import security.auth.AuthConfigImpl
import services.users.UserService
import utils.Response

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-04-24.
 *
 */

class UserController @Inject() (val userService: UserService)(implicit ec: ExecutionContext)
    extends Controller with LoginLogout with AuthConfigImpl {

  /**
   * Sign up for social user, with expries is 7 days.
   * Or merge a user from guest to social with a provide `muid`.
   * Or merge a user form social to another social with a provide `msuid`.
   * No need for login, just sign up again when token is expries.
   */
  def signupSocial(token: Int) = Action.async(parse.json) { implicit request =>
    request.body.validate[UserSocial] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(userSocial, _) if userSocial.muid.isDefined =>
        userService.updateUserRowByUid(userSocial.muid.get, UserRowHelpers.from(userSocial)).flatMap {
          case Some(userRow) => gotoLoginSucceededBuilder(userRow, token)
          case _             => Future.successful(ServerError(userSocial.toString))
        }
      case JsSuccess(userSocial, _) =>
        val suid = if (userSocial.msuid.isDefined) userSocial.msuid.get else userSocial.suid
        userService.updateUserRowBySuid(suid, UserRowHelpers.from(userSocial)).flatMap {
          case Some(userRow) => gotoLoginSucceededBuilder(userRow, token)
          case _ =>
            userService.insert(UserRowHelpers.from(userSocial)).flatMap {
              case Some(userRow) => gotoLoginSucceededBuilder(userRow, token)
              case _             => Future.successful(ServerError(userSocial.toString))
            }
        }
    }
  }

  /**
   * Sign up for guest user, with expries is 7 days.
   */
  def signupGuest(token: Int) = Action.async(parse.json) { implicit request =>
    request.body.validate[UserGuest] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(userGuest, _) =>
        userService.insert(UserRowHelpers.from(userGuest)).flatMap {
          case Some(userRow) => gotoLoginSucceededBuilder(userRow, token)
          case _             => Future.successful(ServerError(userGuest.toString))
        }
    }
  }

  /**
   * Login for guest with `uid` and `password`.
   */
  def loginGuest(token: Int) = Action.async(parse.json) { implicit request =>
    request.body.validate[UserLoginInfo] match {
      case err @ JsError(_)                                                => Future.successful(JsonInvalidError(err))
      case JsSuccess(info, _) if info.uid.isEmpty && info.password.isEmpty => Future.successful(DataInvalidError("Invalid UserLoginInfo"))
      case JsSuccess(info, _) => userService.authenticate(info).flatMap {
        case Some(userRow) => gotoLoginSucceededBuilder(userRow, token)
        case _             => Future.successful(AuthVerifyError(info.toString))
      }
    }
  }

  private def gotoLoginSucceededBuilder(userRow: UserRow, tokenFlag: Int)(implicit request: RequestHeader): Future[Result] = {
    val userRep: UserResponse = UserRowHelpers.toResponse(userRow)
    val uid: Long = userRep.uid
    val response = gotoLoginSucceeded(uid, Future.successful(ServerSucced(userRep)))
    tokenFlag match {
      case 0 => response
      case _ => response.map { result =>
        val token: Option[String] = result.header.headers.get("Authorization")
        ServerSucced(userRep, token = token).withHeaders(("Authorization", token.getOrElse("Basic ")))
      }
    }
  }

  /**
   * Sign up for local user with `email` and password, will send a email to your email address with a verification,
   * Post the verification to API to make sure the email address is in use.
   */

  /**
   * Verification vrify with `email` and `verification`, when succeed, go to login succeed.
   */

  /**
   * Login for local user with `email` and `password`.
   */

  /**
   * Base info change for local user. This must be the current user.
   */

  def afterLogin() = Action.async { implicit request =>
    Future(
      ServerSucced("LoginSucceed")
    )
  }

  def afterLogout() = Action.async { implicit request =>
    Future(
      ServerSucced("LoginSucceed")
    )
  }
}

