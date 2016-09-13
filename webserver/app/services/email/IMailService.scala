package services.email

/**
 * Created by zhangshl on 16/9/13.
 */

import javax.inject.Inject
import com.google.inject.ImplementedBy
import org.apache.commons.mail.{ DefaultAuthenticator, Email, SimpleEmail }
import utils.EmailConfig._

@ImplementedBy(classOf[MailService])
trait IMailService {
  def resetPassword(emailAddress: String, password: String): Boolean
  def welcome(emailAddress: String): Boolean
}

class MailService @Inject() () extends IMailService {
  def resetPassword(emailAddress: String, password: String): Boolean = {
    try {
      val email: Email = new SimpleEmail()
      email.setHostName(hostname)
      email.setSmtpPort(post)
      email.setAuthenticator(new DefaultAuthenticator(username, passwordstr))
      email.setSSLOnConnect(true)
      email.setFrom(from)
      email.setSubject(subject)
      email.setMsg(password)
      email.addTo(emailAddress)
      email.send()
      true
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def welcome(emailAddress: String): Boolean = {
    try {
      val email: Email = new SimpleEmail()
      email.setHostName(hostname)
      email.setSmtpPort(post)
      email.setAuthenticator(new DefaultAuthenticator(username, passwordstr))
      email.setSSLOnConnect(true)
      email.setFrom(from)
      email.setSubject(subject)
      email.setMsg(welcomestr)
      email.addTo(emailAddress)
      email.send()
      true
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }
}

//object TestMail {
//  def main(args: Array[String]) {
//
//    try {
//      val email: Email = new SimpleEmail()
//      email.setHostName("smtp.126.com")
//      email.setSmtpPort(465)
//      email.setAuthenticator(new DefaultAuthenticator("lieyingbj", "jiama369"))
//      email.setSSLOnConnect(true)
//      email.setFrom("lieyingbj@126.com")
//      email.setSubject("奇点资讯新密码")
//      email.setMsg("123456")
//      email.addTo("5420020721@qq.com")
//      email.send()
//    } catch {
//      case e: Exception => e.printStackTrace()
//    }
//
//  }
//}