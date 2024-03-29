package commons.utils

import java.util.UUID

import org.apache.commons.codec.digest.DigestUtils

/**
 * Created by zhange on 2016-04-26.
 *
 */

trait PasswordUtils {
  val STRETCH_LOOP_COUNT = 1000

  def hashAndStretch(plain: String, salt: String, loopCnt: Int): String = {
    var hashed: String = ""
    (1 to STRETCH_LOOP_COUNT).foreach(i =>
      hashed = DigestUtils.sha256Hex(hashed + plain + salt)
    )
    hashed
  }

  def createPasswordSalt(): String = {
    DigestUtils.sha256Hex(UUID.randomUUID().toString)
  }
}

object PasswordUtils extends PasswordUtils
