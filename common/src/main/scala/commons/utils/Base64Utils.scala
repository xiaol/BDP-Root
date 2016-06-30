package commons.utils

import java.nio.charset.Charset

import org.apache.commons.codec.binary.Base64

/**
 * Created by zhange on 16/4/7.
 *
 */

trait Base64Utils {

  def encodeBase64(string: String): String =
    Base64.encodeBase64String(string.getBytes("UTF-8")).replaceAll("=", "")

  def decodeBase64(string: String): String =
    new String(Base64.decodeBase64(string), Charset.forName("UTF-8"))
}

object Base64Utils extends Base64Utils
