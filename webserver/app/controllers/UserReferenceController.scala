package controllers

import javax.inject.Inject

import commons.models.users._
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.users.{ PersonaService, UserReferenceService, UserService }
import utils.Response._

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by zhangsl on 2016-12-01.
 *
 */

class UserReferenceController @Inject() (val userReferenceService: UserReferenceService, val userService: UserService, val personaService: PersonaService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def getPersonaByUid(uid: Long) = Action.async { request =>
    personaService.getPersonaByUid(uid: Long).map {
      case Some(persona: Persona) => ServerSucced(persona)
      case _                      => DataEmptyError(s"$uid")
    }
  }

  def findByUid(uid: String, sys_source: String) = Action.async { request =>
    userReferenceService.findByUid(uid: String, sys_source: String).map {
      case Some(userReferenceRow: UserReferenceRow) => ServerSucced(userReferenceRow)
      case _                                        => DataEmptyError(s"$uid, $sys_source")
    }
  }

  def findByGlobal_id(global_id: String) = Action.async { request =>
    userReferenceService.findByGlobal_id(global_id).map {
      case Some(userReferenceRow: UserReferenceRow) => ServerSucced(userReferenceRow)
      case _                                        => DataEmptyError(s"$global_id")
    }
  }

  def insert(uid: String, uname: Option[String], sys_source: String) = Action.async { request =>
    val regex: String = "^[a-z0-9A-Z]+"
    uid.matches(regex) match {
      case true => sys_source.matches(regex) match {
        case true => userReferenceService.insert(uid, uname, sys_source).map {
          case Some(userReferenceRow: String) => ServerSucced(userReferenceRow)
          case _                              => DataCreateError(s"$uid, $uname, $sys_source")
        }
        case _ => Future.successful(DataInvalidError(s"$sys_source"))
      }
      case _ => Future.successful(DataInvalidError(s"$uid"))
    }
  }

  def update(global_id: String, uid: String, uname: Option[String], sys_source: String) = Action.async { request =>
    val regex: String = "^[a-z0-9A-Z]+"
    uid.matches(regex) match {
      case true => sys_source.matches(regex) match {
        case true => userReferenceService.update(global_id: String, uid: String, uname: Option[String], sys_source: String).map {
          case Some(userReferenceRow: UserReferenceRow) => ServerSucced(userReferenceRow)
          case _                                        => DataCreateError(s"$global_id, $uid, $uname, $sys_source")
        }
        case _ => Future.successful(DataInvalidError(s"$sys_source"))
      }
      case _ => Future.successful(DataInvalidError(s"$uid"))
    }
  }

  def delete(global_id: String) = Action.async { request =>
    userReferenceService.delete(global_id: String).map {
      case Some(global_id: String) => ServerSucced(global_id)
      case _                       => DataDeleteError(s"$global_id")
    }
  }
}
