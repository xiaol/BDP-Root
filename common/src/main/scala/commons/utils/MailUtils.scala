package commons.utils

import javax.mail.internet.InternetAddress

import courier.Defaults._
import courier._

/**
 * Created by zhange on 2016-04-26.
 *
 */

trait MailUtils {
  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
  def verifyEmail(email: String): Boolean = emailRegex.findFirstMatchIn(email) match {
    case e @ Some(_) => true
    case _           => false
  }

  def sendMail(to: List[String], cc: List[String], subject: String, content: String) = {
    val sender = "1804615553@qq.com"
    val password = "Jiu4aini58"
    val recipient = "475897864@qq.com"
    val host = "smtp.qq.com"
    val port = 587 // 465

    val mailer = Mailer(host, port)
      .auth(true)
      .as(sender, password)
      .startTtls(true)()

    mailer(Envelope.from(new InternetAddress(sender))
      .to(to.map { t => new InternetAddress(t) }: _*)
      .cc(cc.map { t => new InternetAddress(t) }: _*)
      .cc(new InternetAddress(recipient))
      .subject(subject)
      .content(Text(content)))
  }
}

object MailUtils extends MailUtils
