package controllers

import commons.models.users.UserRow._
import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import commons.models.users._
import play.api.libs.json.Json
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.users.UserService

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-04-21.
 *
 */

class Application @Inject() (val userService: UserService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def listUser() = AsyncStack(AuthorityKey -> AdminiRole) { implicit request =>
    userService.list(1L, 10L).map(u => Ok(Json.toJson(u)))
  }

  def listGuest() = AsyncStack(AuthorityKey -> GuestRole) { implicit request =>
    Future.successful(Ok(Json.toJson("GuestRole")))
  }

  def listSocial() = AsyncStack(AuthorityKey -> RegistRole) { implicit request =>
    Future.successful(Ok(Json.toJson("SocialRole")))
  }

  def listCommon() = Action.async { implicit request =>
    Future.successful(Ok(Json.toJson("NoRole")))
  }
}
