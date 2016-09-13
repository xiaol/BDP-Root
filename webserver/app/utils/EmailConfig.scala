package utils

/**
 * Created by zhange on 2016-04-19.
 *
 */

trait EmailConfig extends Config {

}

object EmailConfig extends EmailConfig {
  val hostname = emailhostname
  val post = emailpost
  val from = emailfrom
  val username = emailusername
  val passwordstr = emailpassword
  val subject = emailsubject
  val welcomestr = emailwelcome
}
