package commons.messages.pipeline

/**
 * Created by zhange on 2016-06-20.
 *
 */

trait ImageTask

case object CreateTask extends ImageTask

case class Download(src: String) extends ImageTask

case class Redownload(src: String) extends ImageTask

case class Discard(src: String) extends ImageTask

case class Crop(src: String) extends ImageTask

case class Clean(path: String) extends ImageTask

case object CleanDir extends ImageTask

case class DetailTasks(tasks: List[String]) extends ImageTask

case class FeedTasks(tasks: List[String]) extends ImageTask

object ImageTask {
  /**
   * Set 30s for each images, at worst, every image will be process for 3 times.
   */
  val assessProcessPeriod = (imgNum: Int, delay: Int) => imgNum * 30 * 3 + delay
}

trait ImageResult

case class Oss(ori: String, oss: String) extends ImageResult

case class FeedOssList(ossList: List[String]) extends ImageResult

case class DetailOssList(ossList: List[Oss]) extends ImageResult

