package proservers.utils

import better.files._
import java.time.Instant

import commons.utils.Md5Utils._

import scala.util.Try
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-07-05.
 *
 */

trait ImageUtils extends Config {

  def formatPath(name: String): String = s"$imageTempPath$name"

  def generatePath(src: String, format: String = ""): String =
    s"$imageTempPath${md5Hash(src, random = true)}$format"

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

  def cleanTemporaryFiles(path: String = imageTempPath, secondsAgo: Int = 10800) = {
    val localDir = File(path)
    Try {
      localDir.listRecursively.filter(f =>
        f.lastModifiedTime.isBefore(Instant.now().minusSeconds(secondsAgo)) // 3 hours ago
      ).foreach(_.delete())
    }
  }
}
