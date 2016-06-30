package actors

import akka.actor._
import akka.event.Logging
import akka.util.Timeout
import akka.pattern.ask
import commons.messages.dispatcher.DispatcherMessage
import utils.Config

import scala.concurrent.duration._
import scala.util.Success
import scala.util.Failure

/**
 * Created by zhange on 2016-05-17.
 *
 */

class SpiderDispatcherServer extends Actor with Config {
  import context.{ dispatcher => contextDispatcher }
  implicit val timeout: Timeout = 10.seconds
  val logger = Logging(context.system, this)
  private val dispatcherPath = dispatcherPathConfig

  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    context.actorSelection(dispatcherPath) ! Identify(dispatcherPath)
    context.system.scheduler.scheduleOnce(5.seconds, self, ReceiveTimeout)
  }

  def receive = identifying

  def identifying: Receive = {
    case ActorIdentity(`dispatcherPath`, Some(dispatcher)) =>
      logger.info("Remote dispatcher is ready now...")
      context.watch(dispatcher)
      context.become(active(dispatcher))
    case ActorIdentity(`dispatcherPath`, None) => logger.info(s"Remote dispatcher not available: $dispatcherPath")
    case ReceiveTimeout                        => sendIdentifyRequest()
    case msg @ _                               => sender ! "DispatcherNotReady"
  }

  def active(dispatcher: ActorRef): Receive = {
    case message: DispatcherMessage => dispatch(sender(), dispatcher, message)
    case Terminated(`dispatcher`) =>
      logger.info("RemoteSchedulerActor terminated")
      sendIdentifyRequest()
      context.become(identifying)
    case _ =>
  }

  private def dispatch(client: ActorRef, dispatcher: ActorRef, msg: DispatcherMessage): Unit = {
    import context.{ dispatcher => contextDispatcher }
    (dispatcher ? msg).onComplete {
      case Success(msgback) => client ! msgback
      case Failure(err) =>
        logger.info(s"DispatcherError:${err.getMessage}")
        client ! s"DispatcherError:${err.getMessage}"
    }
  }
}

object SpiderDispatcherServer {
  def props: Props = Props[SpiderDispatcherServer]
}