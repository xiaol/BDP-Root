package controllers

import javax.inject.Inject

import commons.models.updateversion.UpdateVersion
import jp.t2v.lab.play2.auth.AuthElement
import play.api.libs.json.{ JsError, JsSuccess }
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.updateversion.UpdateVersionService
import services.users.UserService
import utils.Response.{ ServerSucced, _ }

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

/**
 * Created by zhangshl on 17/4/24.
 */
class UpdateController @Inject() (val updateVersionService: UpdateVersionService, val userService: UserService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def compare(uid: Long, channelId: Int, ptype: Int, version: String, version_code: Int) = Action.async { implicit request =>
    updateVersionService.compare(uid: Long, channelId: Int, ptype: Int, version: String, version_code: Int).map {
      case Some(result) if result.updateLog.getOrElse("") != "nonUpdate" => ServerSucced(result)
      case Some(result)                                                  => ServerSucced("no Update！")
      case _                                                             => DataEmptyError(s"$channelId, $ptype, $version, $version_code")
    }
  }

  def insert = Action.async(parse.json) { request =>
    request.body.validate[UpdateVersion] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(updateVersion, _) => updateVersionService.insert(updateVersion).map {
        case flag: Int if flag > 0 => ServerSucced(flag)
        case _                     => DataCreateError(s"$updateVersion")
      }
    }
  }

  def update = Action.async(parse.json) { request =>
    request.body.validate[UpdateVersion] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(updateVersion, _) => updateVersionService.update(updateVersion).map {
        case Some(v) => ServerSucced(v)
        case _       => DataInvalidError(s"$updateVersion")
      }
    }
  }

}