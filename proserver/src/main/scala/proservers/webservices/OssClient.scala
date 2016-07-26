package proservers.webservices

/**
 * Created by zhange on 2016-07-05.
 *
 */

import java.io._

import scala.util.control.NonFatal
import com.aliyun.oss.model.ObjectMetadata
import proservers.utils.OssDriver.ossClient
import akka.event.LoggingAdapter
import proservers.utils.Config

trait OssClient extends Config {

  def ossUpload(path: String)(implicit logger: LoggingAdapter): Option[String] = {
    val imageName = path.split("/").last

    try {
      val file = new File(path)
      val content: InputStream = new FileInputStream(file)
      val meta: ObjectMetadata = new ObjectMetadata()
      meta.setContentLength(file.length())
      meta.setContentType("image/jpeg")

      val result = ossClient.putObject(ossBucketName, imageName, content, meta)

      Some(s"$ossPrefixUri$imageName")
    } catch {
      case NonFatal(e) =>
        logger.error(s"OssClient.ossUpload: $path, ${e.getMessage}")
        None
    }
  }
}
