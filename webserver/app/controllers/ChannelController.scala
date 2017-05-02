package controllers

import javax.inject.Inject

import commons.models.channels.ChannelOrderRow
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc._
import security.auth.AuthConfigImpl
import services.news._
import services.users.UserService
import utils.Response._
import scala.concurrent.ExecutionContext

/**
 * Created by zhangshl on 2017/5/2.
 */

class ChannelController @Inject() (val userService: UserService, val channelOrderService: ChannelOrderService)(implicit ec: ExecutionContext)
    extends Controller with AuthElement with AuthConfigImpl {

  def listByChannel(channel: Int) = Action.async { implicit request =>
    channelOrderService.listByChannel(channel).map {
      case channels: Seq[ChannelOrderRow] if channels.nonEmpty => ServerSucced(channels)
      case _                                                   => DataEmptyError(s"$channel")
    }
  }
}
