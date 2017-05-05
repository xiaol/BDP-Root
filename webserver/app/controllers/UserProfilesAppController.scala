package controllers

import javax.inject.Inject

import commons.models.advertisement.{ Slide, Phone }
import commons.models.userprofiles.UserProfiles
import commons.utils.Base64Utils._
import org.joda.time.LocalDateTime
import play.api.libs.json.{ JsError, JsSuccess }
import play.api.mvc.{ Action, Controller }
import services.userprofiles.UserProfileService
import utils.Response._

import scala.concurrent.{ Future, ExecutionContext }

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
      case JsSuccess(phone, _) => phone match {
        case phone: Phone if phone.uid > 0 => //Future.successful(ServerSucced(phone.uid))
          userProfileService.phone(phone.uid, decodeBase64(phone.b), phone.ctype, phone.province, phone.city, phone.area, phone.ptype, phone.appversion, request.headers.get("X-Real-IP")).map {
            case id: Long if id > 0 => ServerSucced(id)
            case _                  => DataCreateError(s"${phone.toString}")
          }
        case _ => Future.successful(DataCreateError(s"${phone.toString}"))
      }

    }
  }

  def insertSlide(mid: String, uid: Long, ctype: Int, ptype: Int, version_text: Option[String], operate_type: Option[Int]) = Action.async { implicit request =>
    userProfileService.insertSlide(Slide(mid, uid, ctype, ptype, Some(LocalDateTime.now()), request.headers.get("X-Real-IP"), version_text, operate_type)).map {
      case 0L => DataCreateError(s"UserProfilesAppController.insertSlide: $uid, $ctype, $ptype")
      case _  => ServerSucced(uid)
    }
  }
}