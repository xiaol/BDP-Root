package proservers.cores

import akka.actor._
import akka.event._

import scala.concurrent.duration._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import akka.stream._

import scala.util._
import scala.util.control.NonFatal
import org.imgscalr.Scalr
import org.imgscalr.Scalr._
import javax.imageio.ImageIO
import java.io.{ IOException, _ }
import java.net.URLEncoder

import commons.utils.Md5Utils.md5Hash
import commons.utils.UserAgentUtils
import java.util.regex.Pattern
import java.time.Instant

import commons.messages.pipeline._
import ImageProcessor._
import com.aliyun.oss.model.ObjectMetadata
import proservers.utils.OssDriver.ossClient
import proservers.webservices.ImageClient

import scala.concurrent.ExecutionContext

/**
 * Created by zhange on 2016-05-19.
 *
 */

object ImagePipelineServer {
  def props(imageProcessor: ActorRef): Props = Props(new ImagePipelineServer(imageProcessor))
}

class ImagePipelineServer(imageProcessor: ActorRef) extends Actor {
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
        superior ! FeedOssList(for (oss <- successTemp.toList) yield oss.oss)
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

class ImageProcessor()(implicit val mat: Materializer) extends Actor with UserAgentUtils {
  implicit val contextSystem = context.system
  implicit val logger: LoggingAdapter = Logging(contextSystem, this.getClass)
  implicit val ec: ExecutionContext = contextSystem.dispatcher

  val cleanDirTimer: Cancellable = context.system.scheduler.schedule(20.minutes, 60.minutes, self, CleanDir)

  override def receive = {
    case Download(src) => download(src, sender())
    case Crop(path) => crop(path) match {
      case Some(pathC) => upload(pathC) match {
        case Some(oss) => sender ! Oss(pathC, oss)
        case None      => sender ! Discard(pathC)
      }
      case None => sender ! Discard(path)
    }
    case Clean(name) => remove(name)
    case CleanDir    => cleanDir()
    case _           =>
  }

  private def download(src: String, superior: ActorRef) = {
    ImageClient().download(src, followRedirect = true) onComplete {
      case Success(Some(imgBytes)) => save(src, imgBytes) match {
        case Some(path) => upload(path) match {
          case Some(pathU) => superior ! Oss(src, pathU)
          case None        => superior ! Redownload(src)
        }
        case None => superior ! Discard(src)
      }
      case Success(_) => superior ! Discard(src)
      case Failure(err) =>
        logger.error(s"ImageProcessor.DownloadErr: $src, ${err.getMessage}")
        superior ! Redownload(src)
    }
  }

  private def save(src: String, imgBytes: Array[Byte]): Option[String] = {
    try {
      val path: String = formatPath(src = src, format = "")
      val outStream = new BufferedOutputStream(new FileOutputStream(path))
      outStream.write(imgBytes)
      outStream.flush()
      outStream.close()

      val inStream = ImageIO.createImageInputStream(new File(path))
      val readers = ImageIO.getImageReaders(inStream)
      if (readers.hasNext) {
        val reader = readers.next()
        reader.setInput(inStream, true)

        val formatName = reader.getFormatName match {
          case "gif" => ".gif"
          case "png" => ".png"
          case _     => ".jpg"
        }
        val (width, height) = (reader.getWidth(0), reader.getHeight(0))
        reader.dispose()
        inStream.close()

        (width, height) match {
          case (w, h) if (w / h < 7) && w > 100 && h > 75 =>
            val newPath = s"${path}_${w}X$h" + formatName
            val result: Boolean = new File(path).renameTo(new File(newPath))
            if (result) Some(newPath) else None
          case _ => None
        }
      } else {
        inStream.close(); Some(path)
      }
    } catch {
      case NonFatal(e) => logger.error(s"ImageProcessor.SaveErr: $src, ${e.getMessage}"); None
    }
  }

  private def upload(path: String): Option[String] = {
    val bucketName = "bdp-images"
    val imageName = path.split("/").last

    try {
      val file = new File(path)
      val content: InputStream = new FileInputStream(file)
      val meta: ObjectMetadata = new ObjectMetadata()
      meta.setContentLength(file.length())
      meta.setContentType("image/jpeg")

      ossClient.putObject(bucketName, imageName, content, meta)

      // Some(s"http://$bucketName.oss-cn-hangzhou.aliyuncs.com/$imageName")
      Some(s"http://bdp-pic.deeporiginalx.com/$imageName")
    } catch {
      case NonFatal(e) =>
        logger.error(s"ImageProcessor.UploadErr: $path, ${e.getMessage}")
        None
    }
  }

  def crop(path: String): Option[String] = {
    try {
      val imageOpt = path match {
        case p: String if p.endsWith(".gif")                       => convertFormat(path, path.replace(".gif", ".png"), "PNG")
        case p: String if p.endsWith(".jpg") || p.endsWith(".png") => Some(path)
        case _                                                     => None
      }
      imageOpt match {
        case None => None
        case Some(imgPath) =>
          val format = "png" // if (imgPath.endsWith(".png")) "png" else "jpg" -> "jpg" has a BUG to create black images
          val readBufferedImage = ImageIO.read(new File(imgPath))
          val sizeOpt = extractSize(path)
          sizeOpt match {
            case None => None
            case Some((maxWidth, maxHeight)) =>
              createFeatSize(maxWidth, maxHeight) match {
                case Some((w: Int, h: Int)) if w == 0 || w < 100 || maxWidth < 200 || maxHeight < 200 || (maxWidth / maxHeight) > 3 => None
                case Some((w: Int, h: Int)) =>
                  val thumbCrop = Scalr.crop(readBufferedImage, (maxWidth - w) / 2, (maxHeight - h) / 2, w, h, OP_ANTIALIAS)
                  w match {
                    case width if width > 300 =>
                      val thumbPath = formatPath(imgPath, s"_${300}X${225}.$format")
                      val thumb = Scalr.resize(thumbCrop, Method.QUALITY, Mode.FIT_EXACT, 300, 225, OP_ANTIALIAS)
                      val result: Boolean = ImageIO.write(thumb, format, new File(thumbPath))
                      if (result) Some(thumbPath) else None
                    case _ =>
                      val thumbPath = formatPath(imgPath, s"_${w}X$h.$format")
                      val result: Boolean = ImageIO.write(thumbCrop, format, new File(thumbPath))
                      if (result) Some(thumbPath) else None
                  }
                case _ => None
              }
          }
      }
    } catch {
      case NonFatal(e) =>
        logger.error(s"ImageProcessor.CropErr: $path, ${e.getMessage}")
        None
    }
  }

  def convertFormat(localPath: String, newPath: String, formatName: String): Option[String] = {
    try {
      val inputStream = new File(localPath)
      val outputStream = new File(newPath)
      val result: Boolean = ImageIO.write(ImageIO.read(inputStream), formatName, outputStream)
      if (result) Some(newPath) else None
    } catch {
      case NonFatal(e) =>
        logger.error(s"convertFormat with err: ${e.getMessage}, $localPath")
        None
    }
  }
}

object ImageProcessor {
  def props()(implicit mat: Materializer): Props = Props(new ImageProcessor())

  private val pathPrefix = "src/main/resources/images/"

  def formatPath(name: String): String = s"$pathPrefix$name"

  def formatPath(src: String, format: String = ""): String =
    s"$pathPrefix${md5Hash(src, random = true)}$format"

  def encodeSrc(src: String): String = {
    var urlEncoded = src
    val matcher = Pattern.compile("([\\u4e00-\\u9fa5]+)").matcher(src)
    while (matcher.find()) {
      val matchWord = matcher.group(0)
      urlEncoded = urlEncoded.replace(matchWord, URLEncoder.encode(matchWord, "UTF-8"))
    }
    urlEncoded
  }

  def createFeatSize(maxWidth: Int, maxHeight: Int): Option[(Int, Int)] = {
    (for {
      width <- Range(maxWidth, 1, -1)
      height <- Range(maxHeight, 1, -1)
      if (width.toDouble / height) == 4.0 / 3
      if width <= 600 && height <= 600
    } yield (width, height)).headOption
  }

  private val sizeRegex = """_(\d+X\d+)\.""".r

  def extractSize(src: String): Option[(Int, Int)] = {
    try {
      sizeRegex.findFirstMatchIn(src) match {
        case Some(s) if s.groupCount > 0 =>
          val sizes = s.group(1).split("X").map(_.toInt)
          if (sizes.length == 2) Some(sizes.head, sizes.last) else None
        case None => None
      }
    } catch {
      case NonFatal(e) => None
    }
  }

  def remove(name: String): Unit = {
    try {
      val path = new File(formatPath(name))
      if (path.exists() && path.isFile) path.delete()
    } catch {
      case NonFatal(e) =>
    }
  }

  def cleanDir() = {
    import better.files._
    val localDir = "src" / "main" / "resources" / "images"
    try {
      localDir.listRecursively.filter(f =>
        f.lastModifiedTime.isBefore(Instant.now().minusSeconds(60 * 60 * 3)) // 3 hours
      ).foreach(_.delete())
    } catch {
      case NonFatal(e) =>
    }
  }
}
