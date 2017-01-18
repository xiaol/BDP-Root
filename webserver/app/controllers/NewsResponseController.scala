package controllers

import javax.inject.Inject

import commons.models.news._
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.news._
import services.users.UserService
import utils.Response._
import utils.ResponseRecommand.{ DataEmptyError => _, DataInvalidError => _, ServerSucced => _ }

import scala.concurrent.ExecutionContext

/**
 * Created by zhange on 2016-05-16.
 *
 */

class NewsResponseController @Inject() (val userService: UserService, val newsService: NewsResponseService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def news() = Action.async { implicit request =>
    newsService.news().map {
      case news: Seq[SimpleNewsRow] if news.nonEmpty => ServerSucced(news)
      case _                                         => DataEmptyError(s"")
    }

  }

}
