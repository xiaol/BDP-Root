package actors

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.util.Timeout
import services.advertisement.AdResponseService

import scala.concurrent.duration._
import services.community.ASearchService
import services.news._
import commons.models.news.{ NewsPublisherRow, NewsRow }
import commons.models.community.ASearchRows

/**
 * Created by zhange on 2016-05-24.
 *
 */

class PersistanceServer(aSearchService: ASearchService, newsService: NewsService, newsEsService: NewsEsService, newsPublisherService: NewsPublisherService, val adResponseService: AdResponseService) extends Actor {

  import context.dispatcher
  val logger = Logging(context.system, this)
  val timeout: Timeout = 15.seconds

  override def receive = {
    case newsRow: NewsRow if (newsRow.base.content.toString().length > 2) =>
      val superior = sender()
      newsService.insert(newsRow).map {
        case reply if reply.isDefined =>
          val base = newsRow.base
          //插入搜索引擎
          newsEsService.insert(newsRow.copy(base = base.copy(nid = reply)))
          //删除新闻中的广告
          adResponseService.deleteAd(reply.getOrElse(0))
          superior ! reply
        case reply => superior ! reply
      }
    case ASearchRows(aSearchRows) =>
      val superior = sender()
      aSearchService.insertMulti(aSearchRows).map {
        case reply => superior ! reply.size
      }
    case newsPublisherRow: NewsPublisherRow =>
      val superior = sender()
      newsPublisherService.insertOrDiscard(newsPublisherRow).map {
        case reply => superior ! reply
      }
    case msg: String =>
      logger.info(s"CatchMsg: $msg"); sender ! s"CatchMsg: $msg"
    case _ =>
  }
}

object PersistanceServer {
  def props(aSearchService: ASearchService, newsService: NewsService, newsEsService: NewsEsService, newsPublisherService: NewsPublisherService, adResponseService: AdResponseService): Props =
    Props(new PersistanceServer(aSearchService, newsService, newsEsService, newsPublisherService, adResponseService))
}