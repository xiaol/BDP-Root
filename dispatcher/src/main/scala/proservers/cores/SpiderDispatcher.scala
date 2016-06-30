package proservers.cores

import scala.concurrent.duration._
import scala.util.{ Failure, Random, Success }
import akka.actor._
import akka.util.Timeout
import akka.event.Logging
import play.api.libs.json.Json

import commons.utils.MailUtils.sendMail

import commons.messages.dispatcher._
import proservers.utils.RedisDriver.cache

import SpiderDispatcher._

/**
 * Created by zhange on 2016-05-18.
 *
 */

class SpiderDispatcher extends Actor {

  import context.dispatcher
  implicit val timeout: Timeout = 20.seconds
  val log = Logging(context.system, this)

  private val rootPath = "akka://DispatcherSystem/user/SpiderDispatcher/"

  override def receive = {
    case StartDispatcher(init) =>
      createDispatcher(init.sid, init.queue, init.rate, Json.toJson(init.task).toString)
      sender ! DispatchResponse(init)
    case CloseDispatcher(init) =>
      closeDispatcher(init.sid)
      sender ! DispatchResponse(init)
    case ReloadDispatcher(init) =>
      updateDispatcher(init.sid, init.queue, init.rate, Json.toJson(init.task).toString)
      sender ! DispatchResponse(init)
    case StartDispatchers(inits) =>
      inits.foreach { init => createDispatcher(init.sid, init.queue, init.rate, Json.toJson(init.task).toString) }
      sender ! DispatchResponses(inits.map { init =>
        DispatchResponse(init)
      })
    case CloseDispatchers(inits) =>
      inits.foreach { init => closeDispatcher(init.sid) }
      sender ! DispatchResponses(inits.map { init =>
        DispatchResponse(init)
      })
    case ReloadDispatchers(inits) =>
      inits.foreach { init => updateDispatcher(init.sid, init.queue, init.rate, Json.toJson(init.task).toString) }
      sender ! DispatchResponses(inits.map { init =>
        DispatchResponse(init)
      })
    case msg @ _ => println(s"UnKnownMsg:$msg"); sender ! msg
  }

  def createDispatcher(sid: Long, queue: String, rate: Int, task: String): Unit = {
    context.actorSelection(s"$rootPath${sid.toString}").resolveOne().onComplete {
      case Success(actor) => log.info(s"Actor of name: $sid is already exist,don't create.")
      case Failure(ex) =>
        log.info(s"Scheduler with ID: $sid is not exist, now create...")
        val disp = context.actorOf(SpiderDispatcherActor.props, sid.toString)
        disp ! DispatchInfo(queue, rate, task)
    }
  }

  def updateDispatcher(sid: Long, queue: String, rate: Int, task: String): Unit = {
    context.actorSelection(s"$rootPath${sid.toString}").resolveOne().onComplete {
      case Success(actor) =>
        log.info(s"Scheduler with ID: $sid is already exist, now update...")
        actor ! DispatchInfo(queue, rate, task)
      case Failure(ex) =>
        log.info(s"Scheduler with ID: $sid is not exist, now create...")
        val disp = context.actorOf(SpiderDispatcherActor.props, sid.toString)
        disp ! DispatchInfo(queue, rate, task)
    }
  }

  def closeDispatcher(sid: Long): Unit = {
    context.actorSelection(s"$rootPath${sid.toString}").resolveOne().onComplete {
      case Success(actor) =>
        log.info(s"Scheduler with ID: $sid is exist, now close...")
        actor ! DispatchStop
      case Failure(ex) =>
    }
  }
}

object SpiderDispatcher {
  case class DispatchInfo(queue: String, rate: Int, task: String)
  case object DispatchTask
  case object DispatchStop

  def props: Props = Props[SpiderDispatcher]
}

class SpiderDispatcherActor extends Actor {

  import context.dispatcher
  val log = Logging(context.system, this)
  log.debug(self.path.toString)

  private var scheduler: Cancellable = _

  private var currentQueue: Option[String] = None
  private var currentTask: Option[String] = None
  private var currentRate: Option[Int] = None

  override def postStop(): Unit = {
    log.debug(s"Cancel scheduler:$currentQueue")
    scheduler.cancel()
  }

  override def receive = {
    case DispatchInfo(q, r, t) => createTimerDispatcher(q, r, t)
    case DispatchTask =>
      if (currentQueue.isDefined && currentTask.isDefined) {
        cache.lpush(currentQueue.get, currentTask.get).onComplete {
          case Success(size) =>
            if (size > 30L) { sendWarningMail(currentQueue.get, size) }
          //log.info(s"Pop task in sub Scheduler success to queue: ${currentQueue.get}")
          case Failure(err) => log.error(s"Pop task in sub Scheduler failed with err: ${err.getMessage}.")
        }
      }
    case DispatchStop => stopSelf()
    case _            =>
  }

  def createTimerDispatcher(queue: String, rate: Int, task: String) = {
    log.info(s"SubSchedulerActor updating $currentQueue -> $queue, $currentRate -> $rate, $currentTask -> $task")
    if (currentQueue.isDefined && currentTask.isDefined && currentRate.isDefined)
      scheduler.cancel()
    scheduler = context.system.scheduler.schedule(
      initialDelay = Random.nextInt(100).seconds,
      interval = (rate + Random.nextInt(100)).seconds,
      receiver = self,
      message = DispatchTask
    )
    currentQueue = Some(queue)
    currentTask = Some(task)
    currentRate = Some(rate)
  }

  def stopSelf() = context.stop(self)

  def sendWarningMail(queue: String, size: Long) = {
    sendMail(to = List("705834854@qq.com", "283478191@qq.com"), cc = List("114242123@qq.com"),
      subject = "SPIDER-QUEUES-WARNINGS", content = s"Queue: $queue\nSize: $size")
  }
}

object SpiderDispatcherActor {
  def props: Props = Props[SpiderDispatcherActor]
}