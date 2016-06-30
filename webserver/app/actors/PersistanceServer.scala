package actors

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.util.Timeout

import scala.concurrent.duration._
import services.community.ASearchService
import services.news.{ NewsEsService, NewsService }
import commons.models.news.NewsRow
import commons.models.community.ASearchRows

/**
 * Created by zhange on 2016-05-24.
 *
 */

class PersistanceServer(aSearchService: ASearchService, newsService: NewsService, newsEsService: NewsEsService) extends Actor {

  import context.dispatcher
  val logger = Logging(context.system, this)
  val timeout: Timeout = 15.seconds

  override def receive = {
    case newsRow: NewsRow =>
      val superior = sender()
      newsService.insert(newsRow).map {
        case reply if reply.isDefined =>
          val base = newsRow.base
          newsEsService.insert(newsRow.copy(base = base.copy(nid = reply)))
          superior ! reply
        case reply => superior ! reply
      }
    case ASearchRows(aSearchRows) =>
      val superior = sender()
      aSearchService.insertMulti(aSearchRows).map {
        case reply => superior ! reply.size
      }
    case msg: String =>
      logger.info(s"CatchMsg: $msg"); sender ! s"CatchMsg: $msg"
    case _ =>
  }
}

object PersistanceServer {
  def props(aSearchService: ASearchService, newsService: NewsService, newsEsService: NewsEsService): Props =
    Props(new PersistanceServer(aSearchService, newsService, newsEsService))
}