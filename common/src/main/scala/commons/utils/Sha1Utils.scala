package commons.utils

import java.nio.charset.Charset
import java.security.MessageDigest

import org.apache.commons.codec.binary.Base64

/**
 * Created by zhangshl on 16/9/8.
 *
 */

trait Sha1Utils {

  def encodeSha1(decript: String): String = {
    //    val decript: String = "4d14be8c24b0412d91c8d02a9079f713|1473326368"
    val digest: MessageDigest = java.security.MessageDigest.getInstance("SHA-1")
    digest.update(decript.getBytes())
    val messageDigest = digest.digest()
    // Create Hex String
    val hexString: StringBuffer = new StringBuffer()
    // 字节数组转换为 十六进制 数
    val length = messageDigest.length
    for (i <- 0 to length - 1) {
      val shaHex: String = Integer.toHexString(messageDigest(i) & 0xFF)
      if (shaHex.length() < 2) {
        hexString.append(0)
      }
      hexString.append(shaHex)
    }
    hexString.toString()
  }

}

object Sha1Utils extends Sha1Utils
