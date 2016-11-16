package controllers

import commons.models.users.UserRow._
import javax.inject.{ Inject, Singleton }

import jp.t2v.lab.play2.auth.AuthElement
import commons.models.users._
import play.api.mvc._
import play.api.libs.json.{ JsError, JsSuccess }
import security.auth.AuthConfigImpl
import services.news._
import services.users.UserService
import utils.Response._

import scala.concurrent.{ ExecutionContext, Future }
import commons.utils.Base64Utils.decodeBase64
import services.userprofiles.ProfileService
import commons.models.channels.ChannelRow
import commons.models.news.NewsFeedResponse
import commons.models.userprofiles._

/**
 * Created by zhange on 2016-05-24.
 *
 */

@Singleton
class UserProfileController @Inject() (val channelService: ChannelService, val commentService: CommentService, val profileService: ProfileService,
                                       val userService: UserService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def addComment() = Action.async(parse.json) { implicit request => //AsyncStack(parse.json, AuthorityKey -> RegistRole) { implicit request =>
    request.body.validate[CommentRow] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(commentRow, _) => profileService.addComment(commentRow).map {
        case Some(comment) => ServerSucced(comment)
        case _             => DataCreateError(s"${commentRow.toString}")
      }
    }
  }

  def remComment(cid: Long, docid: String) = Action.async { implicit request =>
    profileService.remComment(cid, decodeBase64(docid)).map {
      case Some(c) => ServerSucced(c)
      case _       => DataDeleteError(s"$cid, $docid")
    }
  }

  def listComments(uid: Long, page: Long, count: Long) = Action.async { implicit request =>
    profileService.listCommentsWithNewsInfo(uid, page, count).map {
      case news: Seq[CommentResponse] if news.nonEmpty => ServerSucced(news)
      case _                                           => DataEmptyError(s"$uid, $page, $count")
    }
  }

  def addCommends(cid: Long, uid: Long) = Action.async { implicit request =>
    commentService.findById(cid).flatMap {
      case Some(CommentRow(_, _, _, _, ouid, _, _, _, _, _)) if ouid.isDefined && ouid.get == uid => Future.successful(DataInvalidError("Can not commend your own comment"))
      case Some(_) => profileService.addCommend(cid, uid).map {
        case Some(c) => ServerSucced(c)
        case _       => DataCreateError(s"$cid, $uid")
      }
      case None => Future.successful(DataEmptyError(s"$cid, $uid"))
    }
  }

  def remCommends(cid: Long, uid: Long) = Action.async { implicit request =>
    profileService.remCommend(cid, uid).map {
      case Some(c) => ServerSucced(c)
      case _       => DataDeleteError(s"$cid, $uid")
    }
  }

  def addConcerns(nid: Long, uid: Long) = Action.async { implicit request =>
    profileService.addConcern(nid, uid).map {
      case Some(c) => ServerSucced(c)
      case _       => DataCreateError(s"$nid, $uid")
    }
  }

  def remConcerns(nid: Long, uid: Long) = Action.async { implicit request =>
    profileService.remConcern(nid, uid).map {
      case Some(c) => ServerSucced(c)
      case _       => DataDeleteError(s"$nid, $uid")
    }
  }

  def listConcerns(uid: Long, page: Long, count: Long) = Action.async { implicit request =>
    profileService.listConcerns(uid, page, count).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$uid, $page, $count")
    }
  }

  def addCollects(nid: Long, uid: Long) = Action.async { implicit request =>
    profileService.addCollect(nid, uid).map {
      case Some(c) => ServerSucced(c)
      case _       => DataCreateError(s"$nid, $uid")
    }
  }

  def remCollects(nid: Long, uid: Long) = Action.async { implicit request =>
    profileService.remCollect(nid, uid).map {
      case Some(c) => ServerSucced(c)
      case _       => DataDeleteError(s"$nid, $uid")
    }
  }

  def listCollects(uid: Long, page: Long, count: Long) = Action.async { implicit request =>
    profileService.listCollects(uid, page, count).map {
      case news: Seq[NewsFeedResponse] if news.nonEmpty => ServerSucced(news)
      case _                                            => DataEmptyError(s"$uid, $page, $count")
    }
  }

  def updateUserChannels(uid: Long) = Action.async(parse.json) { implicit request =>
    request.body.validate[UserChannels] match {
      case err @ JsError(_) => Future.successful(JsonInvalidError(err))
      case JsSuccess(channels, _) => profileService.updateUserChannels(uid, channels).map {
        case Some(uchs) => ServerSucced(uchs)
        case _          => DataCreateError(s"$uid, ${channels.toString}")
      }
    }
  }

  def listUserChannels(uid: Long) = Action.async { implicit request =>
    channelService.listByUid(uid).map {
      case channels: Seq[ChannelRow] if channels.nonEmpty => ServerSucced(channels)
      case _                                              => DataEmptyError("")
    }
  }
}
