package proservers.cores

import akka.actor.{ Actor, ActorRef, ActorSystem }
import akka.pattern._
import akka.routing.FromConfig

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Props
import commons.models.community._
import commons.models.news.NewsRow
import org.joda.time.LocalDateTime

/**
 * Created by zhange on 2016-05-24.
 *
 */

class PersistanceServer extends Actor {

  import context.dispatcher

  val persistanceRoutees: ActorRef = context.actorOf(FromConfig.props(), "PersistanceRoutees")
  //context.system.scheduler.schedule(0.seconds, 10.seconds, self, "STORE")
  //context.system.scheduler.schedule(0.seconds, 20.seconds, self, ASearchRows(List(ASearchRow(None, LocalDateTime.now(), "url", ASearch("url", "title", "from", 1, "pname", LocalDateTime.now(), None, None)))))

  override def receive = {
    case newRow: NewsRow =>
      val superior = sender()
      (persistanceRoutees ? newRow)(15.seconds).recover { case _ => None }.map {
        case reply => superior ! reply
      }
    case aSearchRows: ASearchRows =>
      val superior = sender()
      (persistanceRoutees ? aSearchRows)(15.seconds).recover { case _ => 0 }.map {
        case reply => // superior ! reply
      }
    case msg: String => (persistanceRoutees ? msg)(15.seconds).map {
      case msgBack: String => println(s"msgBack:$msgBack")
    }
    case _ =>
  }
}

object PersistanceServer {
  def props: Props = Props[PersistanceServer]
}
