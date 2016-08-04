package proservers.cores

import akka.actor._
import akka.event._

import scala.concurrent.duration._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import scala.util._
import commons.messages.pipeline._
import proservers.utils.ImageUtils
import proservers.cores.ImageDetectIMAJServer.imageContrast

/**
 * Created by zhange on 2016-05-19.
 *
 */

object ImagePipelineServer {
  def props(imageProcessor: ActorRef): Props = Props(new ImagePipelineServer(imageProcessor))
}

class ImagePipelineServer(imageProcessor: ActorRef) extends Actor with ImageUtils {
  import context.dispatcher
  val logger = Logging(context.system, this)

  override def receive = {
    case DetailTasks(tasks) =>
      val superior = sender; processDetailTasks(superior, tasks)
    case FeedTasks(tasks) =>
      val superior = sender; processFeedTasks(superior, tasks)
    case _ =>
  }

  def processDetailTasks(superior: ActorRef, tasks: List[String]) = {
    context.actorOf(Props(new Actor() {
      val tasksQueue: mutable.Queue[String] = mutable.Queue(tasks: _*)
      val successTemp: ArrayBuffer[Oss] = ArrayBuffer[Oss]()
      val discardTemp: ArrayBuffer[String] = ArrayBuffer[String]()

      val timeout: Cancellable =
        context.system.scheduler.scheduleOnce(ImageTask.assessProcessPeriod(tasks.length, 0).seconds, self, ReceiveTimeout)

      val taskGenerator: Cancellable =
        context.system.scheduler.schedule(0.seconds, (20 + Random.nextInt(10)).seconds, self, CreateTask)

      override def receive = {
        case CreateTask => createTask()
        case oss: Oss =>
          successTemp.append(oss); checkReady()
        case Discard(task) =>
          discardTemp.append(task); checkReady()
        case Redownload(task) => tasksQueue.enqueue(task)
        case ReceiveTimeout   => replyAndShutDown()
      }

      def checkReady() = {
        if (tasks.size == (successTemp.size + discardTemp.size)) replyAndShutDown()
        else createTask()
      }

      def replyAndShutDown() = {
        if (tasks.size != (successTemp.size + discardTemp.size))
          logger.error(s"ProcessDetailTasks failed tasks: ${tasks.collect { case task if validate(task) => task }.toString}")
        superior ! DetailOssList(successTemp.toList); context.stop(self)
      }

      def createTask() = {
        if (tasksQueue.isEmpty) tasks.filter(validate).foreach(tasksQueue.enqueue(_))
        if (tasksQueue.nonEmpty) imageProcessor ! Download(tasksQueue.dequeue())
      }

      def validate(task: String): Boolean =
        !successTemp.map(_.ori).contains(task) && !discardTemp.contains(task)

      override def postStop() = {
        timeout.cancel()
        taskGenerator.cancel()
      }
    }))
  }

  def processFeedTasks(superior: ActorRef, tasks: List[String]) = {
    context.actorOf(Props(new Actor() {
      val timeout: Cancellable = context.system.scheduler.scheduleOnce(200.seconds, self, ReceiveTimeout)
      val tasksQueue: mutable.Queue[String] = mutable.Queue(tasks: _*)
      val successTemp: ArrayBuffer[Oss] = ArrayBuffer[Oss]()
      val discardTemp: ArrayBuffer[Discard] = ArrayBuffer[Discard]()

      val taskGenerator: Cancellable = context.system.scheduler.schedule(0.seconds, 5.seconds, self, CreateTask)

      override def receive = {
        case oss: Oss =>
          successTemp.append(oss); checkReady()
        case err: Discard =>
          discardTemp.append(err); checkReady()
        case ReceiveTimeout => replyAndShutDown()
        case CreateTask     => createTask()
      }

      def checkReady() = {
        if (successTemp.size >= 3 || tasks.size == (successTemp.size + discardTemp.size)) replyAndShutDown()
        else createTask()
      }

      def replyAndShutDown() = {
        val temp = successTemp.slice(0, 3)
        val dissimilarities: ArrayBuffer[Oss] = temp.length match {
          case 0 | 1 => temp
          case 2     => temp.slice(1, 2)
          case _ => if (imageContrast(temp.head.ori, temp.tail.head.ori)) {
            temp.slice(1, 2)
          } else temp
        }
        superior ! FeedOssList(for (oss <- dissimilarities.toList) yield oss.oss)
        context.stop(self)
      }

      def createTask() = {
        if (tasksQueue.nonEmpty)
          imageProcessor ! Crop(formatPath(name = tasksQueue.dequeue.split("/").last))
      }

      override def postStop() = {
        timeout.cancel()
        taskGenerator.cancel()
      }
    }))
  }
}
