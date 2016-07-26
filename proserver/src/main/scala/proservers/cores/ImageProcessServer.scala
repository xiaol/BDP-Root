package proservers.cores

import java.io._
import javax.imageio.ImageIO

import akka.actor.{ Actor, ActorRef, Cancellable, Props }
import akka.event.{ Logging, LoggingAdapter }
import akka.stream.Materializer
import commons.messages.pipeline.{ Redownload, _ }
import commons.utils.UserAgentUtils
import proservers.utils.ImageUtils
import proservers.webservices.{ ImageClient, OssClient }
import proservers.cores.ImageDetectIMAJServer.imageDetect

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{ Failure, Success }
import scala.concurrent.duration._

/**
 * Created by zhange on 2016-07-06.
 *
 */

class ImageProcessServer()(implicit val mat: Materializer) extends Actor with UserAgentUtils with OssClient with ImageUtils {
  implicit val contextSystem = context.system
  implicit val logger: LoggingAdapter = Logging(contextSystem, this.getClass)
  implicit val ec: ExecutionContext = contextSystem.dispatcher

  val cleanDirTimer: Cancellable = context.system.scheduler.schedule(20.minutes, 60.minutes, self, CleanDir)

  override def receive = {
    case Download(src) => download(src, sender())
    case Crop(path) => crop(path) match {
      case Some(pathC) => ossUpload(pathC) match {
        case Some(oss) => sender ! Oss(pathC, oss)
        case None      => sender ! Discard(pathC)
      }
      case None => sender ! Discard(path)
    }
    case CleanDir => cleanTemporaryFiles()
    case _        =>
  }

  private def download(src: String, superior: ActorRef) = {
    ImageClient().download(src, followRedirect = true) onComplete {
      case Success(Some(imgBytes)) => save(src, imgBytes) match {
        case Some(path) => ossUpload(path) match {
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
      val path: String = generatePath(src)
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

  def crop(path: String): Option[String] = {
    try {
      val (imageOpt: Option[String], format: String) = path match {
        case p: String if p.endsWith(".gif") => (convertFormat(path, path.replace(".gif", ".png"), "PNG"), "png")
        case p: String if p.endsWith(".jpg") => (Some(path), "jpg")
        case p: String if p.endsWith(".png") => (Some(path), "png")
        case _                               => (None, "unknown")
      }
      imageOpt match {
        case None            => None
        case Some(imagePath) => imageDetect(imagePath, format)
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

object ImageProcessServer {
  def props()(implicit mat: Materializer): Props = Props(new ImageProcessServer())
}