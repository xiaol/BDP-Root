package commons.utils

import java.security.MessageDigest
import java.util.UUID

/**
 * Created by zhange on 2016-06-17.
 *
 */

trait Md5Utils {
  def md5Hash(text: String, random: Boolean = false): String = {
    random match {
      case true =>
        val string = text + UUID.randomUUID().toString.replace("-", "")
        MessageDigest.getInstance("MD5").digest(string.getBytes).map("%02x".format(_)).mkString
      case false =>
        MessageDigest.getInstance("MD5").digest(text.getBytes).map("%02x".format(_)).mkString
    }
  }
}

object Md5Utils extends Md5Utils
