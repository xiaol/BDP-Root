package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.mvc._

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import actors.PersistanceServer
import akka.actor.ActorRef
import akka.routing.FromConfig
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.community.ASearchService
import services.news.{ NewsEsService, NewsService }

/**
 * Created by zhange on 2016-05-24.
 *
 */

@Singleton
class PersistanceController @Inject() (system: ActorSystem, val aSearchService: ASearchService, val newsService: NewsService, val newsEsService: NewsEsService) extends Controller {

  implicit val timeout: Timeout = 20.seconds

  val persistanceRoutees: ActorRef = system.actorOf(FromConfig.props(), "PersistanceRoutees")

  val persistanceServer: ActorRef = system.actorOf(PersistanceServer.props(aSearchService, newsService, newsEsService), "PersistanceServer")

  def testPersistance(msg: String) = Action.async {
    (persistanceServer ? msg).mapTo[String].map { message =>
      Ok(message)
    }
  }
}
