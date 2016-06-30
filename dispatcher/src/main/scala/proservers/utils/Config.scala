package proservers.utils

import com.typesafe.config.ConfigFactory

/**
 * Created by zhange on 2016-05-18.
 *
 */

trait Config {
  val config = ConfigFactory.load()

  val redisConfig = config.getConfig("redis")
  val redisHost = redisConfig.getString("host")
  val redisPort = redisConfig.getInt("port")
  val tryPassword = redisConfig.getString("password")
  val redisPassword: Option[String] = if (tryPassword.nonEmpty) Some(tryPassword) else None
}
