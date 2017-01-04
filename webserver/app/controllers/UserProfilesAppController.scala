package controllers

import javax.inject.Inject

import commons.models.advertisement.Phone
import commons.models.userprofiles.UserProfiles
import commons.utils.Base64Utils._
import play.api.libs.json.{ JsError, JsSuccess }
import play.api.mvc.{ Action, Controller }
import services.userprofiles.UserProfileService
import utils.Response._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zhange on 2016-05-17.
 *
 */

class UserProfilesAppController @Inject() (val userProfileService: UserProfileService)(implicit ec: ExecutionContext)
    extends Controller {

  def insert = Action.async(parse.json) { implicit request =>
    request.body.validate[UserProfiles] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(userProfiles, _) => userProfileService.insert(userProfiles).map {
        case id: Long if id > 0 => ServerSucced(id)
        case _                  => DataCreateError(s"${userProfiles.toString}")
      }
    }
  }

  def phone = Action.async(parse.json) { implicit request =>
    request.body.validate[Phone] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(phone, _) => userProfileService.phone(phone.uid, decodeBase64(phone.b)).map {
        case id: Long if id > 0 => ServerSucced(id)
        case _                  => DataCreateError(s"${phone.toString}")
      }
    }
  }
}