package controllers

import javax.inject.Inject

import commons.models.news.TopicRelationClass
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc.{ Action, Controller }
import security.auth.AuthConfigImpl
import services.news.TopicService
import services.users.UserService
import utils.Response._

import scala.concurrent.ExecutionContext

/**
 * Created by zhangshl on 16/10/17.
 */
class TopicController @Inject() (val userService: UserService, val topicService: TopicService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def topicDetail(tid: Int) = Action.async { implicit request =>
    topicService.topicDetail(tid).map {
      case Some(topicRelationClass: TopicRelationClass) => ServerSucced(topicRelationClass)
      case _                                            => DataEmptyError(s"$tid")
    }
  }

}
